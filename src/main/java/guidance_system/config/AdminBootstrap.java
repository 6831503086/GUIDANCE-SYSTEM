
package guidance_system.config;

import guidance_system.entity.Admin;
import guidance_system.repository.AdminRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {

    @Bean
    public CommandLineRunner createFirstAdmin(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (adminRepository.count() == 0) {

                Admin admin = new Admin();

                admin.setUsername("systemadmin");
                admin.setPasswordHash(
                        passwordEncoder.encode("ChangeMe123!")
                );
                admin.setFullName("System Administrator");
                admin.setAdminRole("SUPER_ADMIN");

                adminRepository.save(admin);

                System.out.println(
                        "First administrator account created successfully."
                );
            }
        };
    }
}
    