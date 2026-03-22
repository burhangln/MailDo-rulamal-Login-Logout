package com.vizja.sw.lab6.lib.security;

import java.util.Optional;

public class SecurityContext {
    private static final ThreadLocal<Authentication>
            AUTHENTICATION_THREAD_LOCAL = new ThreadLocal<>();


    public static void setAuthentication(Authentication authentication) {
        AUTHENTICATION_THREAD_LOCAL.set(authentication);
    }

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(AUTHENTICATION_THREAD_LOCAL.get());
    }


    public static void clear() {
        AUTHENTICATION_THREAD_LOCAL.remove();
    }

}
