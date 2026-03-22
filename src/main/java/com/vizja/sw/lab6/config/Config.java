package com.vizja.sw.lab6.config;

import jakarta.mail.Authenticator;
import jakarta.mail.Session;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class Config {

    private static final Properties mailProps = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                log.error("ERROR: Unable to find application.properties in classpath.");
                throw new RuntimeException("application.properties not found, application cannot start.");
            }
            mailProps.load(input);
            log.info("INFO: SMTP server properties loaded successfully.");
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load application.properties.", exception);
        }
    }

    public static Session getSession() {
       final String smtpUsername = mailProps.getProperty("smtp.username");
       final String smtpPassword = mailProps.getProperty("smtp.password");

        Authenticator authenticator = new Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(smtpUsername, smtpPassword);
            }
        };
        return Session.getInstance(mailProps, authenticator);
    }

}
