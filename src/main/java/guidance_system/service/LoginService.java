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

    public String login(String username, String password) {

        GuidanceTeacher teacher = teacherRepository
                .findByUsername(username)
                .orElse(null);


        if (teacher != null) {

            if (!Boolean.TRUE.equals(teacher.getIsEmailVerified())) {
                throw new IllegalArgumentException(
                        "Please verify your email before logging in."
                );
            }

            if (passwordEncoder.matches(
                    password, teacher.getPasswordHash())) {

                return jwtService.generateToken(username, "TEACHER");
            }

            throw new IllegalArgumentException(
                    "Invalid username or password."
            );
        }

        Admin admin = adminRepository
        .findByUsernameOrEmail(username, username)
        .orElse(null);

        if (admin != null && passwordEncoder.matches(
                password, admin.getPasswordHash())) {

            return jwtService.generateToken(admin.getUsername(), "ADMIN");
        }

        throw new IllegalArgumentException(
                "Invalid username or password."
        );
    }

    
}