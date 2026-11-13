
package guidance_system.service;

import guidance_system.entity.GuidanceTeacher;
import guidance_system.entity.VerificationToken;
import guidance_system.repository.VerificationTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationTokenService(
            VerificationTokenRepository repository) {
        this.repository = repository;
    }

    public VerificationToken createToken(
            GuidanceTeacher teacher, String tokenType) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String tokenValue = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        VerificationToken token = new VerificationToken();
        token.setTeacher(teacher);
        token.setToken(tokenValue);
        token.setTokenType(tokenType);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        return repository.save(token);
    }

    @Transactional
    public Optional<GuidanceTeacher> consumeToken(
            String token, String tokenType) {

        VerificationToken verificationToken =
                repository.findByTokenAndTokenType(token, tokenType)
                        .orElse(null);

        if (verificationToken == null
                || verificationToken.getUsedAt() != null
                || verificationToken.getExpiresAt() == null
                || !verificationToken.getExpiresAt()
                        .isAfter(LocalDateTime.now())) {
            return Optional.empty();
        }

        verificationToken.setUsedAt(LocalDateTime.now());
        repository.save(verificationToken);

        return Optional.of(verificationToken.getTeacher());
    }
}
