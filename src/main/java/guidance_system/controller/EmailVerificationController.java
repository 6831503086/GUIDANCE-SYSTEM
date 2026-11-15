package guidance_system.controller;

import guidance_system.service.GuidanceTeacherService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
            @RequestParam(required = false) String type,
            @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = "") String accept) {

        boolean verified;
        String setupToken = null;
        boolean emailChanged = "change_email".equals(type);

        if (emailChanged) {
            verified = service.verifyEmailChange(token);
        } else if ("verify_email".equals(type)) {
            setupToken = service.verifyRegistrationEmail(token);
            verified = setupToken != null;
        } else {
            setupToken = service.verifyRegistrationEmail(token);
            verified = setupToken != null;
            if (!verified) {
                verified = service.verifyEmailChange(token);
                emailChanged = verified;
            }
        }

        boolean browser = accept.contains(MediaType.TEXT_HTML_VALUE);
        String body = browser ? page(verified, emailChanged, setupToken)
                : verified ? "Email verified successfully."
                : "Invalid, expired, or already used token.";

        return ResponseEntity.status(verified ? 200 : 400)
                .contentType(browser ? MediaType.parseMediaType("text/html;charset=UTF-8")
                        : MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Referrer-Policy", "no-referrer")
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    private String page(boolean verified, boolean emailChanged, String setupToken) {
        String title = verified ? "ยืนยันอีเมลเรียบร้อย" : "ไม่สามารถยืนยันอีเมลได้";
        String message = verified
                ? emailChanged
                    ? "เปลี่ยนอีเมลเรียบร้อยแล้ว<br>กรุณาเข้าสู่ระบบด้วยอีเมลใหม่ของท่าน"
                    : setupToken != null && !setupToken.isEmpty()
                        ? "ยืนยันอีเมลสำเร็จแล้ว<br>ขั้นตอนถัดไป: ตั้งรหัสผ่านและยอมรับเงื่อนไขเพื่อเปิดใช้งานบัญชี"
                        : "ยืนยันอีเมลเรียบร้อยแล้ว<br>กรุณาเข้าสู่ระบบด้วยบัญชีผู้ใช้ที่ลงทะเบียน"
                : "ลิงก์อาจหมดอายุหรือถูกใช้งานแล้ว<br>หากยืนยันอีเมลสำเร็จก่อนหน้านี้ สามารถเข้าสู่ระบบได้";
        String status = verified
                ? "<div class=\"reference-account-status\"><span>สถานะบัญชี</span><strong>ยืนยันอีเมลแล้ว</strong></div>"
                : "";
        String icon = verified
                ? "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" aria-hidden=\"true\"><path d=\"M20 11a8 8 0 1 1-4-6\"/><path d=\"m8 11 4 4 8-10\"/></svg>"
                : "<span aria-hidden=\"true\">!</span>";

        String action = setupToken != null && !setupToken.isEmpty()
                ? "<a href=\"/#registration-setup=" + setupToken + "\" class=\"btn btn-primary reference-submit\">ตั้งรหัสผ่านและเปิดใช้งานบัญชี</a>"
                : "<a href=\"/\" class=\"btn btn-primary reference-submit\">เข้าสู่ระบบ</a>";
        return """
                <!doctype html>
                <html lang="th">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <meta name="referrer" content="no-referrer">
                    <base href="/">
                    <title>%s · Guidance Teacher System</title>
                    <link rel="stylesheet" href="/styles.css">
                    <style>
                        .reference-submit { display:block; text-align:center; text-decoration:none; }
                        .reference-status-icon svg { width:36px; height:36px; }
                        .reference-auth-card { max-width:590px; width:calc(100%% - 32px); box-sizing:border-box; }
                        .reference-status-icon { margin-left:auto; margin-right:auto; }
                        @media (max-width:600px) { .reference-auth-card { padding:28px 20px; } }
                    </style>
                </head>
                <body class="auth-mode">
                    <main id="authRoot" class="auth-reference" data-auth-step="confirmed">
                        <section class="auth-card reference-auth-card" aria-labelledby="verification-title">
                            <div class="reference-status-icon confirmed-icon">%s</div>
                            <h1 id="verification-title">%s</h1>
                            <p class="reference-message">%s</p>
                            <div class="reference-rule"></div>
                            %s
                            %s
                            <p class="reference-support">กรณีไม่สามารถเข้าใช้งานระบบได้ กรุณาติดต่อฝ่ายรับนักศึกษา ส่วนทะเบียนและประมวลผล<br>
                                <a href="tel:053916103">โทร. 0 5391 6103</a>
                            </p>
                        </section>
                    </main>
                </body>
                </html>
                """.formatted(title, icon, title, message, status, action);
    }
}
