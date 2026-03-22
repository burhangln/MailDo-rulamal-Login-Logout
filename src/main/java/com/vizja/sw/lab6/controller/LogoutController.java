package com.vizja.sw.lab6.controller;

import com.vizja.sw.lab6.lib.BaseController;
import com.vizja.sw.lab6.lib.http.Cookie;
import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;
import com.vizja.sw.lab6.lib.http.SessionManager;
import com.vizja.sw.lab6.lib.security.SecurityContext;

public class LogoutController extends BaseController {

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        // Session ID'yi cookie'den al
        String sessionId = request.getCookie(LoginController.SESSION_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(null);

        if (sessionId != null) {
            // Session'ı sil
            SessionManager.removeSession(sessionId);

            // Cookie'yi expire et (maxAge = 0)
            Cookie expiredCookie = new Cookie(LoginController.SESSION_COOKIE_NAME, "")
                    .setPath("/")
                    .setMaxAge(0)
                    .setHttpOnly(true)
                    .setSameSite(Cookie.SameSite.Lax);

            response.addCookie(expiredCookie);
        }

        // SecurityContext temizle
        SecurityContext.clear();

        // Login sayfasına redirect
        response.setStatus(302, "Found");
        response.setHeader("Location", "/login");
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        // İstersen POST /logout da çalışsın
        doGet(request, response);
    }
}
