package guidance_system.repository;

import guidance_system.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("""
        UPDATE Post p
        SET p.isHidden = true,
            p.autoHidden = true
        WHERE p.publishedAt IS NOT NULL
        AND p.visibleEndDate IS NOT NULL
        AND p.visibleEndDate < :today
        AND (p.isHidden = false OR p.isHidden IS NULL)
    """)
    int hideExpiredPosts(@Param("today") LocalDate today);
}