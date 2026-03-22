package com.vizja.sw.lab6.controller;

import com.vizja.sw.lab6.lib.BaseController;
import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;
import com.vizja.sw.lab6.model.Token;
import com.vizja.sw.lab6.model.User;
import com.vizja.sw.lab6.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_CONTENT_TYPE;

public class ActivationController extends BaseController {

    private static final String ACTIVATION_HTML = "src/main/resources/static/activation.html";

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.setHeader(HEADER_CONTENT_TYPE, "text/html; charset=utf-8");
        try {
            String html = Files.readString(Path.of(ACTIVATION_HTML));
            response.getWriter().write(html);
        } catch (IOException e) {
            response.setStatus(500, "Internal Server Error");
            response.getWriter().println("Error loading activation page");
        }
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        Optional<String> usernameOpt = request.getParam("username");
        Optional<String> codeOpt = request.getParam("code");

        if (usernameOpt.isEmpty() || codeOpt.isEmpty()) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Username and activation code are required");
            return;
        }

        String username = usernameOpt.get();
        String code = codeOpt.get();

        Optional<User> userOpt = UserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("User not found");
            return;
        }

        User user = userOpt.get();
        Token token = user.activationToken();

        if (token == null) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Activation token is missing");
            return;
        }

        if (Instant.now().isAfter(token.expiryTimestamp())) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Activation token has expired");
            return;
        }

        if (!token.value().equals(code)) {
            response.setStatus(400, "Bad Request");
            response.getWriter().println("Invalid activation code");
            return;
        }

        // Kullanıcıyı aktive et (activationToken'ı null yapıyoruz)
        User activatedUser = new User(
                user.username(),
                user.hashPassword(),
                true,
                user.role(),
                null
        );
        UserRepository.update(activatedUser);

        // Login sayfasına, activated=true ile yönlendir (login.html bunu gösteriyor)
        response.setStatus(302, "Found");
        response.setHeader("Location", "/login?activated=true");
    }
}
