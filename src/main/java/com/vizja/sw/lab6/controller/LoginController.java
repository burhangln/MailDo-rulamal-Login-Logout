package com.vizja.sw.lab6.controller;

import com.vizja.sw.lab6.lib.BaseController;
import com.vizja.sw.lab6.lib.http.Cookie;
import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;
import com.vizja.sw.lab6.lib.http.Session;
import com.vizja.sw.lab6.lib.http.SessionManager;
import com.vizja.sw.lab6.lib.security.Authentication;
import com.vizja.sw.lab6.lib.security.SecurityContext;
import com.vizja.sw.lab6.lib.security.SecurityUtil;
import com.vizja.sw.lab6.model.User;
import com.vizja.sw.lab6.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.vizja.sw.lab6.lib.http.HttpUtil.HEADER_CONTENT_TYPE;

public class LoginController extends BaseController {

    public static final String SESSION_COOKIE_NAME = "SESSIONID";
    private static final String LOGIN_HTML = "src/main/resources/static/login.html";

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.setHeader(HEADER_CONTENT_TYPE, "text/html; charset=utf-8");
        try {
            String html = Files.readString(Path.of(LOGIN_HTML));
            response.getWriter().write(html);
        } catch (IOException e) {
            response.setStatus(500, "Internal Server Error");
            response.getWriter().println("Error loading login page");
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

        String username = usernameOpt.get();
        String password = passwordOpt.get();

        Optional<User> userOpt = UserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.setStatus(401, "Unauthorized");
            response.getWriter().println("Invalid credentials");
            return;
        }

        User user = userOpt.get();

        if (!user.isActivated()) {
            response.setStatus(403, "Forbidden");
            response.getWriter().println("Account is not activated");
            return;
        }

        boolean passwordMatches = SecurityUtil.verifyPassword(password, user.hashPassword());
        if (!passwordMatches) {
            response.setStatus(401, "Unauthorized");
            response.getWriter().println("Invalid credentials");
            return;
        }

        // SESSION oluştur
        Session session = SessionManager.createSession();
        session.setAttribute("username", user.username());
        session.setAttribute("role", user.role());

        // SecurityContext'i doldur (bu request için)
        SecurityContext.setAuthentication(new Authentication(user.username(), user.role()));

        // Session cookie gönder
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, session.getId())
                .setPath("/")
                .setHttpOnly(true)
                .setSameSite(Cookie.SameSite.Lax);

        response.addCookie(cookie);

        // Basit bir "welcome" sayfası (istersen burada redirect de yapabiliriz)
        response.setHeader(HEADER_CONTENT_TYPE, "text/html; charset=utf-8");
        response.getWriter().println("""
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><title>Welcome</title></head>
                <body>
                    <h2>Welcome, %s!</h2>
                    <p>You are in side.</p>
                    <p><a href="/logout">Logout</a></p>
                </body>
                </html>
                """.formatted(user.username()));
    }
}
