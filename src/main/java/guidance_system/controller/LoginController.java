
package guidance_system.controller;

import guidance_system.service.LoginService;
import guidance_system.service.GuidanceTeacherService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
                    request.email(),
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
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/password-reset-options")
    public java.util.List<GuidanceTeacherService.ResetSchoolOption> passwordResetOptions() {
        return teacherService.passwordResetOptions();
    }

    // Request password reset
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        boolean sent = teacherService.requestPasswordReset(
                request.email(), request.schoolId(), request.provinceName()
        );

        if (!sent) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "RESET_DETAILS_MISMATCH",
                    "message", "อีเมลหรือข้อมูลโรงเรียนไม่ตรงกับบัญชีที่ลงทะเบียนและยืนยันแล้ว กรุณาตรวจสอบอีกครั้ง"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "message", "ส่งลิงก์รีเซ็ตรหัสผ่านแล้ว กรุณาตรวจสอบกล่องจดหมายและสแปม"
        ));
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(@Valid @RequestBody CompleteRegistrationRequest request) {
        try {
            boolean completed = teacherService.completeRegistration(request.token(), request.password(),
                    request.confirmPassword(), request.pdpaConsent());
            if (!completed) return ResponseEntity.badRequest().body(Map.of("message",
                    "Invalid, expired, or already used setup link. Please return to the setup page opened from your email."));
            return ResponseEntity.ok(Map.of("message", "Registration complete. You can now log in."));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
        }
    }

    public record CompleteRegistrationRequest(@NotBlank String token, @NotBlank String password,
            @NotBlank String confirmPassword, @jakarta.validation.constraints.NotNull Boolean pdpaConsent) {}

    @PostMapping("/reset-password-status")
    public ResponseEntity<?> resetPasswordStatus(@RequestBody ResetPasswordStatusRequest request) {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(Map.of("valid", teacherService.isPasswordResetTokenValid(request.token())));
    }

    public record ResetPasswordStatusRequest(String token) {}

    // Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        try {
            boolean success = teacherService.resetPassword(
                    request.token(), request.newPassword(), request.pdpaConsent());
            if (!success) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid or expired reset link."));
            }
            return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
        }
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Please provide a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Positive
        Long schoolId,
        @NotBlank String provinceName
      ) {
      }

    public record ResetPasswordRequest(
            @NotBlank(message = "Token is required")
            String token,

            @NotBlank(message = "New password is required")
            String newPassword,
            @jakarta.validation.constraints.NotNull(message = "PDPA consent is required")
            Boolean pdpaConsent
    ) {
    }
}
