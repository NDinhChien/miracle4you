package mfy.server.domain.user.service;

import java.util.HashMap;
import java.util.Map;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.core.io.Resource;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private static final String SENDER_NAME = "Miracle4You's team";
    private static final String NOREPLY_ADDRESS = "noreply@miracle4you";

    private final JavaMailSender emailSender;

    private final SpringTemplateEngine thymeleafTemplateEngine;

    private final TextTemplate textTemplate;

    @Value("classpath:/mail-logo.png")
    private Resource resourceFile;

    public void sendAccountVerification(User recipient, String token, Boolean html) {
        String to = recipient.getEmail();
        String name = StringUtils.hasText(recipient.getFullName()) ? recipient.getFullName() : recipient.getNickname();
        String text = textTemplate.getAccountVerificationText(to, token);
        String subject = textTemplate.getAccountVerificationSubject();
        if (html) {
            sendMessageUsingThymeleafTemplate(to, name, subject, text);
            return;
        } else {
            sendSimpleMessage(to, subject, text);
        }
    }

    public void sendPasswordReset(User recipient, String token, Boolean html) {
        String to = recipient.getEmail();
        String name = StringUtils.hasText(recipient.getFullName()) ? recipient.getFullName() : recipient.getNickname();
        String text = textTemplate.getPasswordResetText(to, token);
        String subject = textTemplate.getPasswordResetSubject();
        if (html) {
            sendMessageUsingThymeleafTemplate(to, name, subject, text);
            return;
        } else {
            sendSimpleMessage(to, subject, text);
        }
    }

    private void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(NOREPLY_ADDRESS);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
        } catch (Exception e) {
            log.info("Failed to send email: {}", e.getMessage());
        }
    }

    private void sendMessageUsingThymeleafTemplate(String to, String recipientName, String subject, String text) {

        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("senderName", SENDER_NAME);
            templateModel.put("recipientName", recipientName);
            templateModel.put("text", text);
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            String htmlBody = thymeleafTemplateEngine.process("template-thymeleaf.html", thymeleafContext);

            sendHtmlMessage(to, subject, htmlBody);
        } catch (Exception e) {
            log.info("Failed to send email: {}", e.getMessage());
        }
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(NOREPLY_ADDRESS);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.addInline("attachment.png", resourceFile);
        emailSender.send(message);
    }

}
