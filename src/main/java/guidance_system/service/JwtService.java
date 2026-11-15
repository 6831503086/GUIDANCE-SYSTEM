
package guidance_system.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final guidance_system.repository.GuidanceTeacherRepository teachers;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs,
            guidance_system.repository.GuidanceTeacherRepository teachers) {
        this.teachers=teachers;

        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes."
            );
        }

        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        this.jwtEncoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(key)
        );

        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, String role) {

        Instant now = Instant.now();

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("guidance-system")
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .subject(email)
                .claim("role", role);
        if("TEACHER".equals(role))builder.claim("teacherId",teachers.findByEmail(email)
            .orElseThrow(()->new IllegalArgumentException("Teacher account no longer exists.")).getTeacherId());
        JwtClaimsSet claims=builder.build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
