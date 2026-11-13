package guidance_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GuidanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                GuidanceSystemApplication.class, args);
    }
}
