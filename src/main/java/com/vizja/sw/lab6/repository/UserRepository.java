package com.vizja.sw.lab6.repository;



import com.vizja.sw.lab6.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    public static void registerUser(User user) {
        USERS.put(user.username(), user);
    }

    public static Optional<User> findByUsername(String username) {
        return Optional.ofNullable(USERS.get(username));
    }

    public static void update(User user) {
        USERS.put(user.username(), user);
    }

}
