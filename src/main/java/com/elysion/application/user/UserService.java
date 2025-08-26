package com.elysion.application.user;

import com.elysion.domain.user.User;
import com.elysion.security.PasswordService;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    PasswordService passwordService;

    @Inject
    MailService mailService;

    public User register(String email, String plainPassword, String firstName, String lastName) {
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

        // Neu:
        user.firstName = firstName;
        user.lastName = lastName;

        // ✨ DOI
        user.active = false;
        user.activationToken = UUID.randomUUID().toString();
        user.activationTokenCreated = OffsetDateTime.now();

        user.persist();

        mailService.sendActivationMail(user); // ✉️ Dummy-Funktion, siehe unten

        return user;
    }

    public void changeEmail(User user, String newEmail) {
        if (User.find("email", newEmail).firstResult() != null ||
                User.find("pendingEmail", newEmail).firstResult() != null) {
            throw new IllegalArgumentException("E-Mail already in use");
        }
        // Statt sofortiges Überschreiben:
        user.pendingEmail = newEmail;
        user.activationToken = UUID.randomUUID().toString();
        user.activationTokenCreated = OffsetDateTime.now();
        user.persist();

        // E-Mail an die neue Adresse senden
        mailService.sendEmailChangeConfirmation(user);
    }


    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordService.verifyPassword(currentPassword, user.salt, user.passwordHash)) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        String newSalt = passwordService.generateSalt();
        String newHash = passwordService.hashPassword(newPassword, newSalt);
        user.salt = newSalt;
        user.passwordHash = newHash;
        user.persist();
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

    /** Erzeugt ein JWT mit 2-Stunden-Laufzeit und der Rolle aus dem User-Objekt */
    public String generateJwt(User user) {
        Set<String> groups = new HashSet<>();
        groups.add("User"); // Basisrolle immer
        if (user.role != null && !"User".equals(user.role)) {
            groups.add(user.role); // z.B. "Seller" oder "Admin"
        }

        return Jwt.issuer("elysion-user-service")
                .upn(user.email)
                .subject(user.id.toString())
                .groups(groups)
                .audience("elysion-product-service")
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

    @Transactional
    public User confirmEmail(String token) {
        User user = User.find("activationToken", token).firstResult();
        if (user == null) {
            throw new IllegalArgumentException("Invalid token");
        }
        if (user.activationTokenCreated.isBefore(OffsetDateTime.now().minusHours(24))) {
            throw new IllegalStateException("Token expired");
        }

        if (user.pendingEmail != null) {
            user.email = user.pendingEmail;
            user.pendingEmail = null;
        }
        user.active = true;
        user.activationToken = null;
        user.activationTokenCreated = null;
        user.persist();
        return user;
    }

    @Transactional
    public User promoteToSeller(UUID userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (!user.active) {
            throw new IllegalStateException("User not activated");
        }
        user.role = "Seller";
        user.persist();
        return user;
    }

    @Transactional
    public User promoteToAdmin(UUID targetUserId) {
        User u = User.findById(targetUserId);
        if (u == null) throw new IllegalArgumentException("User not found");
        if (!u.active) throw new IllegalStateException("User not activated");
        u.role = "Admin";
        u.persist();
        return u;
    }

    @Transactional
    public void resendActivationToken(String email) {
        User user = User.find("email", email).firstResult();
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (user.active) {
            throw new IllegalStateException("Account already activated");
        }

        // Optional: Rate Limit (z. B. nicht öfter als alle 15 Minuten)
        if (user.activationTokenCreated != null &&
                user.activationTokenCreated.isAfter(OffsetDateTime.now().minusMinutes(15))) {
            throw new IllegalStateException("Token was recently sent");
        }

        user.activationToken = UUID.randomUUID().toString();
        user.activationTokenCreated = OffsetDateTime.now();
        user.persist();

        mailService.sendActivationMail(user);
    }
}