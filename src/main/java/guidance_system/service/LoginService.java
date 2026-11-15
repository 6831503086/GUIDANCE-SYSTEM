
package guidance_system.service;

import guidance_system.entity.Admin;
import guidance_system.entity.GuidanceTeacher;
import guidance_system.repository.AdminRepository;
import guidance_system.repository.GuidanceTeacherRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final GuidanceTeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(
            GuidanceTeacherRepository teacherRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {

        GuidanceTeacher teacher = teacherRepository
                .findByEmail(email)
                .orElse(null);

        if (teacher != null) {

            if (!Boolean.TRUE.equals(teacher.getIsEmailVerified())) {
                throw new IllegalArgumentException(
                        "Please verify your email before logging in."
                );
            }

            if (!Boolean.TRUE.equals(teacher.getPdpaConsent())) {
                throw new IllegalArgumentException("Please complete password setup and PDPA consent before logging in.");
            }

            if (passwordEncoder.matches(
                    password, teacher.getPasswordHash())) {

                return jwtService.generateToken(email, "TEACHER");
            }

            throw new IllegalArgumentException(
                    "Invalid email or password."
            );
        }

        Admin admin = adminRepository
                .findByEmail(email)
                .orElse(null);

        if (admin != null && passwordEncoder.matches(
                password, admin.getPasswordHash())) {

            return jwtService.generateToken(email, "ADMIN");
        }

        throw new IllegalArgumentException(
                "Invalid email or password."
        );
    }
}
