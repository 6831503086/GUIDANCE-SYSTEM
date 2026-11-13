
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

    // Register teacher and generate email verification token
    
    // Register teacher and generate email verification token
    @Transactional
    public GuidanceTeacher registerTeacher(GuidanceTeacher teacher) {

        // Check PDPA consent
        if (!Boolean.TRUE.equals(teacher.getPdpaConsent())) {
            throw new IllegalArgumentException(
                    "PDPA consent is required before registration.");
        }

        // Check password
        if (teacher.getPasswordHash() == null
                || teacher.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        // Check duplicate username
        if (repository.existsByUsername(teacher.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        // Check duplicate email
        if (repository.existsByEmail(teacher.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // Encrypt password
        teacher.setPasswordHash(
                passwordEncoder.encode(teacher.getPasswordHash())
        );

        teacher.setIsEmailVerified(false);
        teacher.setRegisteredAt(LocalDate.now());

        GuidanceTeacher savedTeacher = repository.save(teacher);

        // Generate verification token
        VerificationToken token = tokenService.createToken(
                savedTeacher, "verify_email"
        );

        // Send verification email
        emailService.sendVerificationEmail(
                savedTeacher.getEmail(),
                token.getToken()
        );

        return savedTeacher;
    }


    @Transactional
    public boolean verifyTeacherEmail(String token) {

        return tokenService.consumeToken(token, "verify_email")
                .map(teacher -> {
                    teacher.setIsEmailVerified(true);
                    repository.save(teacher);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void requestEmailChange(
        String username,
        String newEmail) {

    if (newEmail == null || newEmail.isBlank()) {
        throw new IllegalArgumentException("New email is required.");
    }

    if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
    throw new IllegalArgumentException("Please enter a valid email address.");
    }

    GuidanceTeacher teacher = repository.findByUsername(username)
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
        String username,
        GuidanceTeacher details,
        Integer targetAcademicYear) {

    GuidanceTeacher teacher = repository.findByUsername(username)
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


    // Request password reset
    @Transactional
    public void requestPasswordReset(
            Long schoolId,
            String email) {

        if (schoolId == null || email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "School and email are required.");
        }

        GuidanceTeacher teacher = repository
                .findBySchool_SchoolIdAndEmail(schoolId, email)
                .orElse(null);

        // Do not reveal whether the account exists
        if (teacher == null) {
            return;
        }

        VerificationToken token = tokenService.createToken(
                teacher, "password_reset");

        emailService.sendPasswordResetEmail(
                teacher.getEmail(),
                token.getToken());
    }

    // Set a new password using the reset token
    @Transactional
    public boolean resetPassword(
            String token,
            String newPassword) {

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        return tokenService.consumeToken(token, "password_reset")
                .map(teacher -> {
                    teacher.setPasswordHash(
                            passwordEncoder.encode(newPassword));

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
