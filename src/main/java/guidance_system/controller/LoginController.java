
package guidance_system.controller;

import guidance_system.service.LoginService;
import guidance_system.service.GuidanceTeacherService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;
    private final GuidanceTeacherService teacherService;

    public LoginController(
            LoginService loginService,
            GuidanceTeacherService teacherService) {
        this.loginService = loginService;
        this.teacherService = teacherService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        try {
            String token = loginService.login(
                    request.username(),
                    request.password()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "token", token
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password."));
        }
    }

    // Request password reset
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        teacherService.requestPasswordReset(
                request.schoolId(),
                request.email()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If the information matches an account, a password reset email will be sent."
                )
        );
    }

    // Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        boolean success = teacherService.resetPassword(
                request.token(),
                request.newPassword()
        );

        if (!success) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid or expired reset link.")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Password reset successfully.")
        );
    }

    public record LoginRequest(
            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record ForgotPasswordRequest(
            Long schoolId,

            @NotBlank(message = "Email is required")
            String email
    ) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Token is required")
            String token,

            @NotBlank(message = "New password is required")
            String newPassword
    ) {
    }
}
