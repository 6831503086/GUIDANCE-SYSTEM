package guidance_system.controller;

import guidance_system.repository.GuidanceTeacherRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Read-only receipts, separate from email verification and login tokens. */
@RestController
@RequestMapping("/api/auth/registration-verification")
public class RegistrationVerificationController {
    private record Receipt(Long teacherId, Instant expiresAt) {}
    private final ConcurrentHashMap<String, Receipt> receipts = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final GuidanceTeacherRepository repository;

    public RegistrationVerificationController(GuidanceTeacherRepository repository) {
        this.repository = repository;
    }

    public String issue(Long teacherId) {
        Instant now = Instant.now();
        receipts.entrySet().removeIf(e -> !e.getValue().expiresAt().isAfter(now));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        receipts.put(token, new Receipt(teacherId, now.plusSeconds(86400)));
        return token;
    }

    public record CheckRequest(String checkToken) {}

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Boolean>> check(@RequestBody CheckRequest request) {
        String token = request.checkToken();
        Receipt receipt = token == null || token.length() != 43 ? null : receipts.get(token);
        if (receipt == null || !receipt.expiresAt().isAfter(Instant.now())) {
            if (token != null) receipts.remove(token);
            return ResponseEntity.status(410).header("Cache-Control", "no-store")
                    .body(Map.of("verified", false));
        }
        boolean verified = repository.findById(receipt.teacherId())
                .map(t -> Boolean.TRUE.equals(t.getIsEmailVerified())).orElse(false);
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(Map.of("verified", verified));
    }
}
