package guidance_system.repository;

import guidance_system.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(
            String token, String tokenType);

    @Transactional
    void deleteByTeacher_TeacherId(Long teacherId);
    
}