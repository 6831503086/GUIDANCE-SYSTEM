
package guidance_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.verification.base-url}")
    private String verificationBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String email, String token) {

        String verificationLink =
                verificationBaseUrl
                + "/api/email-verification/verify?token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Guidance System - Email Verification");
        message.setText(
                "Thank you for registering with the Guidance Teacher System.\n\n"
                + "Please click the link below to verify your email:\n\n"
                + verificationLink
                + "\n\nThis link expires in 24 hours."
        );

        mailSender.send(message);
    }


    public void sendPasswordResetEmail(String email, String token) {

    String resetLink =
            verificationBaseUrl
            + "/?token="
        + token;

    SimpleMailMessage message = new SimpleMailMessage();

    message.setTo(email);
    message.setSubject("Guidance System - Password Reset");
    message.setText(
            "We received a request to reset your Guidance System password.\n\n"
            + "Please open the link below to reset your password:\n\n"
            + resetLink
            + "\n\nThis link expires in 24 hours.\n"
            + "If you did not request this, please ignore this email."
    );

    mailSender.send(message);
    }

}
