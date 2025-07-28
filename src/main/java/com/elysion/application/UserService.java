package com.elysion.application;

import com.elysion.domain.User;
import com.elysion.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    PasswordService passwordService;

    public User register(String email, String plainPassword) {
        if (User.find("email", email).firstResult() != null) {
            throw new IllegalArgumentException("E-Mail already in use");
        }

        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(plainPassword, salt);

        User user = new User();
        user.id = UUID.randomUUID();
        user.email = email;
        user.salt = salt;
        user.passwordHash = hash;
        user.createdAt = OffsetDateTime.now();

        user.persist();

        return user;
    }

    public User authenticate(String email, String plainPassword) {
        User user = User.find("email", email).firstResult();
        if (user == null || !passwordService.verifyPassword(plainPassword, user.salt, user.passwordHash)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }
}