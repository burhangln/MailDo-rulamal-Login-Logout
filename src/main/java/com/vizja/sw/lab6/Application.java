package com.vizja.sw.lab6;

import com.vizja.sw.lab6.controller.ActivationController;
import com.vizja.sw.lab6.controller.LoginController;
import com.vizja.sw.lab6.controller.LogoutController;
import com.vizja.sw.lab6.controller.RegistrationController;
import com.vizja.sw.lab6.lib.BaseController;
import com.vizja.sw.lab6.lib.FrontController;
import com.vizja.sw.lab6.lib.Server;
import com.vizja.sw.lab6.lib.filter.Filter;
import com.vizja.sw.lab6.lib.filter.FilterChain;
import com.vizja.sw.lab6.lib.http.Cookie;
import com.vizja.sw.lab6.lib.http.HttpRequest;
import com.vizja.sw.lab6.lib.http.HttpResponse;
import com.vizja.sw.lab6.lib.http.SessionManager;
import com.vizja.sw.lab6.lib.security.Authentication;
import com.vizja.sw.lab6.lib.security.Role;
import com.vizja.sw.lab6.lib.security.SecurityContext;

import java.util.List;

public class Application {
    public static void main(String[] args) {

        // --- ROUTES ---
        FrontController.addRoute("/register", new RegistrationController());
        FrontController.addRoute("/activation", new ActivationController());
        FrontController.addRoute("/login", new LoginController());
        FrontController.addRoute("/logout", new LogoutController());

        // Ana sayfayı login'e yönlendirelim
        FrontController.addRoute("/", new BaseController() {
            @Override
            public void doGet(HttpRequest request, HttpResponse response) {
                response.setStatus(302, "Found");
                response.setHeader("Location", "/login");
            }
        });

        // --- FILTERS ---
        Filter loggingFilter = (request, response, chain) -> {
            long start = System.currentTimeMillis();
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - start;
            System.out.printf("[%s] %s %s -> %d %s (%d ms)%n",
                    request.getClientIp(),
                    request.getMethod(),
                    request.getPath(),
                    response.getStatusCode(),
                    response.getStatusMessage(),
                    duration);
        };

        // Session'dan SecurityContext dolduran basit auth filter ----
        Filter authFilter = (request, response, chain) -> {
            request.getCookie(LoginController.SESSION_COOKIE_NAME)
                    .map(Cookie::getValue)
                    .flatMap(SessionManager::getSession)
                    .ifPresent(session -> {
                        Object usernameAttr = session.getAttribute("username");
                        Object roleAttr = session.getAttribute("role");

                        if (usernameAttr instanceof String username && roleAttr instanceof Role role) {
                            SecurityContext.setAuthentication(new Authentication(username, role));
                        }
                    });

            chain.doFilter(request, response);
        };

        List<Filter> filters = new FilterChain.Builder()
                .addFilter(loggingFilter)
                .addFilter(authFilter)
                .build();

        FrontController.registerFilterChain(filters);

        // --- SERVER START ---
        try (var server = new Server()) {
            server.start(8080);
        }
    }
}
