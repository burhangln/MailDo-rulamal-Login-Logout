package com.vizja.sw.lab6.model;


import com.vizja.sw.lab6.lib.security.Role;

public record User(
        String username,
        String hashPassword,
        boolean isActivated,
        Role role,
        Token activationToken
) {
}
