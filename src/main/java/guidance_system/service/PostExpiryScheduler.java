package guidance_system.service;

import guidance_system.repository.PostRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class PostExpiryScheduler {

    private final PostRepository repository;

    public PostExpiryScheduler(PostRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void hideExpiredPosts() {
        System.out.println("===== POST EXPIRY SCHEDULER STARTED =====");

        int count = repository.hideExpiredPosts(LocalDate.now());

        System.out.println("Expired posts automatically hidden: " + count);
        System.out.println("===== POST EXPIRY SCHEDULER FINISHED =====");
    }
}