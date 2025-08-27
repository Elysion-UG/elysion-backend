package com.elysion.application.user;

import com.elysion.domain.user.User;
import com.elysion.domain.user.UserToken;
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
        user.firstName = firstName;
        user.lastName = lastName;
        user.active = false;
        user.persist();

        // Activation-Token ausstellen
        UserToken t = new UserToken();
        t.id = UUID.randomUUID();
        t.user = user;
        t.type = "ACTIVATION";
        t.token = UUID.randomUUID().toString();
        t.createdAt = OffsetDateTime.now();
        t.persist();

        mailService.sendActivationMail(user, t.token); // ✉️ Dummy-Funktion, siehe unten

        return user;
    }

    public void changeEmail(User user, String newEmail) {
        if (User.find("email", newEmail).firstResult() != null ||
                User.find("pendingEmail", newEmail).firstResult() != null) {
            throw new IllegalArgumentException("E-Mail already in use");
        }
        user.pendingEmail = newEmail;
        user.persist();

        // offenes EMAIL_CHANGE-Token je User erzwingen
        UserToken.update("usedAt = ?1 WHERE user = ?2 AND type = ?3 AND usedAt IS NULL",
                OffsetDateTime.now(), user, "EMAIL_CHANGE");

        UserToken t = new UserToken();
        t.id = UUID.randomUUID();
        t.user = user;
        t.type = "EMAIL_CHANGE";
        t.token = UUID.randomUUID().toString();
        t.createdAt = OffsetDateTime.now();
        t.persist();

        // E-Mail an die neue Adresse senden
        mailService.sendEmailChangeConfirmation(user, t.token);
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
    public void confirmEmail(String rawToken) {
        String token = rawToken == null ? null : rawToken.trim();
        UserToken t = UserToken.find("lower(token) = lower(?1) AND type = ?2 AND usedAt IS NULL",
                token, "ACTIVATION").firstResult();
        if (t == null) throw new IllegalArgumentException("Invalid token");

        User user = t.user;

        if (!user.active) {
            user.active = true;
            user.persist();
        }
        t.confirmedAt = OffsetDateTime.now();
        t.persist();
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
        if (user == null) throw new IllegalArgumentException("User not found");
        if (user.active) throw new IllegalStateException("Account already activated");

        // alte offenen ACTIVATION-Tokens schließen
        UserToken.update("usedAt = ?1 WHERE user = ?2 AND type = ?3 AND usedAt IS NULL",
                OffsetDateTime.now(), user, "ACTIVATION");

        UserToken t = new UserToken();
        t.id = UUID.randomUUID();
        t.user = user;
        t.type = "ACTIVATION";
        t.token = UUID.randomUUID().toString();
        t.createdAt = OffsetDateTime.now();
        t.persist();

        mailService.sendActivationMail(user, t.token);
    }

    @Transactional
    public String loginWithIdentToken(String rawToken) {
        String token = rawToken == null ? null : rawToken.trim();
        UserToken t = UserToken.find("lower(token) = lower(?1) AND type = ?2 AND usedAt IS NULL",
                token, "ACTIVATION").firstResult();
        if (t == null) throw new IllegalArgumentException("Invalid token");
        if (t.confirmedAt == null || !t.user.active)
            throw new IllegalStateException("Account not activated");

        // optionales Fenster nach Confirm (z.B. 15 Min):
        // if (t.confirmedAt.isBefore(OffsetDateTime.now().minusMinutes(15)))
        //     throw new IllegalStateException("Token exchange window expired");

        t.usedAt = OffsetDateTime.now();
        t.persist();
        return generateJwt(t.user);
    }
}