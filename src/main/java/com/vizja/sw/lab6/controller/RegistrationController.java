package com.vizja.sw.lab6.controller;

import com.vizja.sw.lab6.lib.BaseController;
import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;
import com.vizja.sw.lab6.lib.security.Role;
import com.vizja.sw.lab6.lib.security.SecurityUtil;
import com.vizja.sw.lab6.model.Token;
import com.vizja.sw.lab6.model.User;
import com.vizja.sw.lab6.repository.UserRepository;
import com.vizja.sw.lab6.service.MailService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_CONTENT_TYPE;

public class RegistrationController extends BaseController {

    private static final String REGISTRATION_HTML = "src/main/resources/static/registration.html";
    private final MailService mailService = new MailService();

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.setHeader(HEADER_CONTENT_TYPE, "text/html; charset=utf-8");
        try {
            String html = Files.readString(Path.of(REGISTRATION_HTML));
            response.getWriter().write(html);
        } catch (IOException e) {
            response.setStatus(500, "Internal Server Error");
            response.getWriter().println("Error loading registration page");
        }
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        Optional<String> usernameOpt = request.getParam("username");
        Optional<String> passwordOpt = request.getParam("password");

        if (usernameOpt.isEmpty() || passwordOpt.isEmpty()) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Username and password are required");
            return;
        }

        String username = usernameOpt.get().trim();
        String password = passwordOpt.get();

        if (username.isEmpty() || password.isEmpty()) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Username and password cannot be empty");
            return;
        }

        if (UserRepository.findByUsername(username).isPresent()) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("User already exists");
            return;
        }

        // Şifreyi hashle
        String hashedPassword = SecurityUtil.hashPassword(password);

        // Aktivasyon kodu/token üret
        String activationCode = mailService.generateActivationCode();
        Token token = new Token(
                activationCode,
                Instant.now().plus(15, ChronoUnit.MINUTES) // 15 dakika geçerli
        );

        User user = new User(
                username,
                hashedPassword,
                false,          // isActivated = false
                Role.USER,
                token
        );

        // Kullanıcıyı kaydet
        UserRepository.registerUser(user);

        // Aktivasyon maili gönder
        mailService.sendActivationEmail(username, activationCode);

        // Aktivasyon sayfasına yönlendir
        response.setStatus(302, "Found");
        response.setHeader("Location", "/activation?username=" + username);
    }
}
