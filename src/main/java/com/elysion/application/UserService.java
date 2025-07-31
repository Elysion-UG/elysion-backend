package com.elysion.application;

import com.elysion.domain.User;
import com.elysion.security.PasswordService;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    PasswordService passwordService;

    @Inject
    MailService mailService;

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
        user.role = "User";

        // ✨ DOI
        user.active = false;
        user.activationToken = UUID.randomUUID().toString();
        user.activationTokenCreated = OffsetDateTime.now();

        user.persist();

        mailService.sendActivationMail(user); // ✉️ Dummy-Funktion, siehe unten

        return user;
    }

    public User authenticate(String email, String plainPassword) {
        User user = User.find("email", email).firstResult();
        if (user == null || !passwordService.verifyPassword(plainPassword, user.salt, user.passwordHash)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!user.active) {
            throw new IllegalArgumentException("Account not activated");
        }
        return user;
    }

    /** Erzeugt ein JWT mit 2‑Stunden‑Laufzeit und Gruppe "User" */
    public String generateJwt(User user) {
        return Jwt.issuer("my-issuer")
                .upn(user.email)
                .groups(Set.of(user.role))
                .expiresIn(Duration.ofHours(2))
                .sign();
    }

    /**
     * Sucht einen User anhand seiner E‑Mail-Adresse.
     *
     * @param email Die E‑Mail, nach der gesucht wird.
     * @return Der gefundene User oder null, wenn keiner existiert.
     */
    public User findByEmail(String email) {
        return User.find("email", email).firstResult();
    }
}