package guidance_system.controller;

import guidance_system.service.GuidanceTeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email-verification")
@CrossOrigin(origins = "*")
public class EmailVerificationController {

    private final GuidanceTeacherService service;

    public EmailVerificationController(GuidanceTeacherService service) {
        this.service = service;
    }

   @GetMapping("/verify")
    public ResponseEntity<String> verify(
        @RequestParam String token,
        @RequestParam(required = false) String type) {

    boolean verified;

    if ("change_email".equals(type)) {
        verified = service.verifyEmailChange(token);
    } else if ("verify_email".equals(type)) {
        verified = service.verifyTeacherEmail(token);
    } else {
        verified = service.verifyTeacherEmail(token);

        if (!verified) {
            verified = service.verifyEmailChange(token);
        }
    }

    if (!verified) {
        return ResponseEntity.badRequest()
                .body("Invalid, expired, or already used token.");
    }

    return ResponseEntity.ok("Email verified successfully.");
}
}