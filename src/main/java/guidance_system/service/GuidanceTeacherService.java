package guidance_system.service;

import guidance_system.entity.GuidanceTeacher;
import guidance_system.entity.VerificationToken;
import guidance_system.repository.GuidanceTeacherRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import guidance_system.repository.VerificationTokenRepository;

import guidance_system.repository.PostPinRepository;
import guidance_system.repository.PostRegistrationRepository;

import java.time.LocalDate;
import guidance_system.entity.School;

@Service
public class GuidanceTeacherService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private final GuidanceTeacherRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService tokenService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PostPinRepository postPinRepository;
    private final PostRegistrationRepository postRegistrationRepository;

    public GuidanceTeacherService(
        GuidanceTeacherRepository repository,
        PasswordEncoder passwordEncoder,
        VerificationTokenService tokenService,
        EmailService emailService,
        VerificationTokenRepository verificationTokenRepository,
        PostPinRepository postPinRepository,
        PostRegistrationRepository postRegistrationRepository)  {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.postPinRepository = postPinRepository;
        this.postRegistrationRepository = postRegistrationRepository;
    }

    // Validate and create only registration fields; ignore client-supplied account state.
    @Transactional
    public GuidanceTeacher registerTeacher(GuidanceTeacher request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration details are required.");
        }
        String email = request.getEmail() == null ? ""
                : request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        if (email.length() > 150 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        String firstName = request.getFirstName() == null ? "" : request.getFirstName().trim();
        String lastName = request.getLastName() == null ? "" : request.getLastName().trim();
        String position = request.getPositionTitle() == null ? "" : request.getPositionTitle().trim();
        String phone = request.getPhone() == null ? "" : request.getPhone().trim();
        if (firstName.isEmpty() || lastName.isEmpty() || position.isEmpty()
                || firstName.length() > 100 || lastName.length() > 100 || position.length() > 100) {
            throw new IllegalArgumentException("First name, last name and position are required (maximum 100 characters each).");
        }
        if (!phone.matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException("Please enter a valid 10-digit phone number starting with 0.");
        }
        if (request.getSchool() == null || request.getSchool().getSchoolId() == null
                || request.getPrefix() == null || request.getPrefix().getPrefixId() == null) {
            throw new IllegalArgumentException("Please select an existing school and prefix.");
        }
        // Serialize concurrent registrations for the same school until transaction completion.
        School school = entityManager.find(School.class, request.getSchool().getSchoolId(),
                jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        guidance_system.entity.Prefix prefix = entityManager.find(
                guidance_system.entity.Prefix.class, request.getPrefix().getPrefixId());
        if (school == null || prefix == null) {
            throw new IllegalArgumentException("The selected school or prefix no longer exists.");
        }
        if (repository.existsBySchool_SchoolId(school.getSchoolId())) {
            throw new IllegalArgumentException("This school already has a guidance teacher account. Please contact the administrator.");
        }
        if (repository.findAll().stream().anyMatch(existing ->
                email.equalsIgnoreCase(existing.getEmail())
                || email.equalsIgnoreCase(existing.getPendingEmail()))) {
            throw new IllegalArgumentException("Email is already registered or awaiting verification.");
        }
        GuidanceTeacher teacher = new GuidanceTeacher();
        teacher.setSchool(school);
        teacher.setPrefix(prefix);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setPositionTitle(position);
        teacher.setPhone(phone);
        teacher.setEmail(email);
        // No client password is accepted before email verification.
        teacher.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        teacher.setPdpaConsent(false);
        teacher.setIsEmailVerified(false);
        teacher.setRegisteredAt(LocalDate.now());
        teacher.setActiveAcademicYear(LocalDate.now().getYear() + 543);
        GuidanceTeacher savedTeacher = repository.save(teacher);
        VerificationToken token = tokenService.createToken(savedTeacher, "verify_email");
        emailService.sendVerificationEmail(savedTeacher.getEmail(), token.getToken());
        return savedTeacher;
    }

    /** The setup credential is issued only to the holder of the email link. */
    @Transactional
    public String verifyRegistrationEmail(String token) {
        var verified = tokenService.consumeToken(token, "verify_email");
        if (verified.isEmpty()) return null;
        GuidanceTeacher teacher = entityManager.find(GuidanceTeacher.class,
                verified.get().getTeacherId(), jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (teacher == null) return null;
        teacher.setIsEmailVerified(true);
        repository.save(teacher);
        if (Boolean.TRUE.equals(teacher.getPdpaConsent())) return ""; // Legacy registration.
        return tokenService.createToken(teacher, "registration_setup").getToken();
    }

    @Transactional
    public boolean completeRegistration(String token, String password, String confirmPassword, Boolean consent) {
        if (!Boolean.TRUE.equals(consent)) {
            throw new IllegalArgumentException("PDPA consent is required.");
        }
        if (password == null || password.length() < 8
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
                || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords must match and contain at least 8 characters (maximum 72 UTF-8 bytes).");
        }
        var pending = tokenService.consumeToken(token, "registration_setup");
        if (pending.isEmpty()) return false;
        GuidanceTeacher teacher = entityManager.find(GuidanceTeacher.class,
                pending.get().getTeacherId(), jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (teacher == null || !Boolean.TRUE.equals(teacher.getIsEmailVerified())
                || Boolean.TRUE.equals(teacher.getPdpaConsent())) return false;
        teacher.setPasswordHash(passwordEncoder.encode(password));
        teacher.setPdpaConsent(true);
        repository.save(teacher);
        return true;
    }

    @Transactional
    public void requestEmailChange(
        String currentEmail,
        String newEmail) {

    if (newEmail == null || newEmail.isBlank()) {
        throw new IllegalArgumentException("New email is required.");
    }

    if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
    throw new IllegalArgumentException("Please enter a valid email address.");
    }

    GuidanceTeacher teacher = repository.findByEmail(currentEmail)
            .orElseThrow(() ->
                    new IllegalArgumentException("Teacher not found."));

    if (newEmail.equalsIgnoreCase(teacher.getEmail())) {
        throw new IllegalArgumentException(
                "New email is the same as current email.");
    }

    if (repository.existsByEmail(newEmail)) {
        throw new IllegalArgumentException(
                "Email is already registered.");
    }

    teacher.setPendingEmail(newEmail);
    repository.save(teacher);

    VerificationToken token =
            tokenService.createToken(teacher, "change_email");

    emailService.sendVerificationEmail(
            newEmail,
            token.getToken()
    );
    }

    @Transactional
    public boolean verifyEmailChange(String token) {

        return tokenService.consumeToken(token, "change_email")
                .map(teacher -> {

                    String pendingEmail = teacher.getPendingEmail();

                    if (pendingEmail == null || pendingEmail.isBlank()) {
                        return false;
                    }

                    if (repository.existsByEmail(pendingEmail)) {
                        return false;
                    }

                    teacher.setEmail(pendingEmail);
                    teacher.setPendingEmail(null);
                    teacher.setIsEmailVerified(true);

                    repository.save(teacher);

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public GuidanceTeacher renewAcademicYear(
        String email,
        GuidanceTeacher details,
        Integer targetAcademicYear) {

    GuidanceTeacher teacher = repository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("Teacher not found."));

    if (!Boolean.TRUE.equals(teacher.getIsEmailVerified())) {
        throw new IllegalArgumentException(
                "Please verify your email before renewing.");
    }

    if (targetAcademicYear == null ||
            targetAcademicYear != teacher.getActiveAcademicYear() + 1) {
        throw new IllegalArgumentException(
                "You can only renew for the next academic year.");
    }

    if (details.getPrefix() == null ||
            details.getPrefix().getPrefixId() == null ||
            details.getFirstName() == null ||
            details.getFirstName().isBlank() ||
            details.getLastName() == null ||
            details.getLastName().isBlank()) {

        throw new IllegalArgumentException(
                "Prefix, first name and last name are required.");
    }

    if (details.getSchool() == null ||
            details.getSchool().getName() == null ||
            details.getSchool().getName().isBlank() ||
            details.getSchool().getSchoolCode() == null ||
            details.getSchool().getSchoolCode().isBlank() ||
            details.getSchool().getSchoolType() == null ||
            details.getSchool().getSchoolType().isBlank()) {

        throw new IllegalArgumentException(
                "School name, school code and school type are required.");
    }

    teacher.setPrefix(details.getPrefix());
    teacher.setFirstName(details.getFirstName());
    teacher.setLastName(details.getLastName());
    teacher.setPhone(details.getPhone());
    teacher.setPositionTitle(details.getPositionTitle());

    School school = teacher.getSchool();

    if (school == null) {
        throw new IllegalArgumentException(
                "Teacher does not have an assigned school.");
    }

    school.setName(details.getSchool().getName());
    school.setSchoolCode(details.getSchool().getSchoolCode());
    school.setSchoolType(details.getSchool().getSchoolType());

    teacher.setActiveAcademicYear(targetAcademicYear);

    return repository.save(teacher);
    }


    public record ResetSchoolOption(Long schoolId, String name, String provinceName) {}

    @Transactional(readOnly = true)
    public java.util.List<ResetSchoolOption> passwordResetOptions() {
        // Scalar projection: one query, no per-school entity or account lookups.
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT s.school_id, s.name, COALESCE(p.name, 'ไม่ระบุจังหวัด')
                FROM school s
                LEFT JOIN subdistrict sd ON sd.subdistrict_id = s.subdistrict_id
                LEFT JOIN district d ON d.district_id = sd.district_id
                LEFT JOIN province p ON p.province_id = d.province_id
                ORDER BY s.name, s.school_id
                """).getResultList();
        return rows.stream().map(row -> new ResetSchoolOption(
                ((Number) row[0]).longValue(), (String) row[1], (String) row[2])).toList();
    }

    // Request password reset
    @Transactional
    public boolean requestPasswordReset(String email, Long schoolId, String provinceName) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required.");
        }

        GuidanceTeacher teacher = repository
                .findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                .orElse(null);

        // Only eligible accounts with matching school details can request a reset.
        if (teacher == null || !Boolean.TRUE.equals(teacher.getIsEmailVerified())
                || !Boolean.TRUE.equals(teacher.getPdpaConsent())
                || teacher.getSchool() == null || schoolId == null
                || !schoolId.equals(teacher.getSchool().getSchoolId())) {
            return false;
        }
        School school = teacher.getSchool();
        String actualProvince = "ไม่ระบุจังหวัด";
        if (school.getSubdistrict() != null
                && school.getSubdistrict().getDistrict() != null
                && school.getSubdistrict().getDistrict().getProvince() != null) {
            actualProvince = school.getSubdistrict().getDistrict().getProvince().getName();
        }
        // A province mismatch must not be reported as a successful request.
        if (!actualProvince.equals(provinceName)) {
            return false;
        }

        VerificationToken token = tokenService.createToken(
                teacher, "password_reset");

        emailService.sendPasswordResetEmail(
                teacher.getEmail(),
                token.getToken());
        return true;
    }


    // Set a new password using the reset token
    /** Read-only preview; never consumes or extends the token. */
    @Transactional(readOnly = true)
    public boolean isPasswordResetTokenValid(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return false;
        return entityManager.createQuery("""
                SELECT COUNT(v) FROM VerificationToken v JOIN v.teacher t
                WHERE v.token = :token AND v.tokenType = 'password_reset'
                  AND v.usedAt IS NULL AND v.expiresAt > :now
                  AND t.isEmailVerified = true AND t.pdpaConsent = true
                """, Long.class)
                .setParameter("token", token)
                .setParameter("now", java.time.LocalDateTime.now())
                .getSingleResult() > 0;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword, Boolean pdpaConsent) {
        if (!Boolean.TRUE.equals(pdpaConsent)) {
            throw new IllegalArgumentException("PDPA consent is required before resetting your password.");
        }
        if (newPassword == null || newPassword.length() < 8
                || newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain at least 8 characters and at most 72 UTF-8 bytes.");
        }
        return tokenService.consumeToken(token, "password_reset")
                .map(teacher -> {
                    // Recovery must not activate an unverified or incomplete registration.
                    if (!Boolean.TRUE.equals(teacher.getIsEmailVerified())
                            || !Boolean.TRUE.equals(teacher.getPdpaConsent())) return false;
                    teacher.setPasswordHash(passwordEncoder.encode(newPassword));
                    teacher.setPdpaConsent(true);
                    repository.save(teacher);
                    return true;
                })
                .orElse(false);
    }


    @Transactional
    public void deleteTeacherAccount(Long teacherId) {

        GuidanceTeacher teacher = repository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        // Delete all verification and password-reset tokens first
        verificationTokenRepository.deleteByTeacher_TeacherId(teacherId);

        postPinRepository.deleteByTeacherTeacherId(teacherId);

        postRegistrationRepository.deleteByTeacher_TeacherId(teacherId);

        // Delete the teacher account
        repository.delete(teacher);
    }



}
