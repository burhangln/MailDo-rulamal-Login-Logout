package com.vizja.sw.lab6.service;


import com.vizja.sw.lab6.config.Config;
import com.vizja.sw.lab6.lib.security.SecurityUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

@Slf4j
public class MailService {
    protected static final String CONTENT_TYPE_TEXT_HTML = "text/html; charset=utf-8";
    private static final String subject = "Account activation";
    private static final String ACTIVATION_HTML_EMAIL_TEMPLATE = "src/main/resources/static/activation_email.html";
    public static final String ACCOUNT_ACTIVATION_PATH = "http://localhost:8080/activation?username=";

    public void sendActivationEmail(String email, String activationCode) {
        Session session = Config.getSession();
        var message = new MimeMessage(session);

        try {
            message.setSubject(subject);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            String formattedHtml = getHtmlEmailContent(activationCode, email);
            message.setContent(formattedHtml, CONTENT_TYPE_TEXT_HTML);
            log.info("Sending activation email to: {}", email);
            Transport.send(message);
            log.info("Activation email sent successfully to {}", email);
        } catch (MessagingException | IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    private String getHtmlEmailContent(String activationCode, String email) throws MessagingException, IOException {
        String template = Files.readString(Path.of(ACTIVATION_HTML_EMAIL_TEMPLATE));
        return template
                .replace("{{activationCode}}", activationCode)
                .replace("{{activationLink}}", ACCOUNT_ACTIVATION_PATH + email);

    }

    public String generateActivationCode() {
        var secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[6];
        secureRandom.nextBytes(tokenBytes);
        return SecurityUtil.base64Encoding(tokenBytes);
    }
}
