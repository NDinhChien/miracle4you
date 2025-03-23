package mfy.server.domain.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;

@Component
@Getter
public class TextTemplate {

    @Value("${client.url}")
    private String clientUrl;

    private final String accountVerificationSubject = "Verify Your Account";

    @Getter(AccessLevel.NONE)
    private final String accountVerificationTemplateText = """
                Welcome to our website, \\n
                The verification code for your email (%s) is %s \\n
            """;
    private final String passwordResetSubject = "Reset Your Password";

    @Getter(AccessLevel.NONE)
    private final String passwordResetTemplateText = """
                To reset password for your account (%s), follows this link: %s \\n
                If you did not request, please ignore this email. \\n
                Thanks.
            """;

    public String getAccountVerificationText(String to, String token) {
        String link = String.format(clientUrl + "/auth/verify-email?token=%s", token);
        return String.format(accountVerificationTemplateText, to, link);
    }

    public String getPasswordResetText(String to, String token) {
        String link = String.format(clientUrl + "/auth/reset-password?token=%s", token);
        return String.format(passwordResetTemplateText, to, link);
    }
}
