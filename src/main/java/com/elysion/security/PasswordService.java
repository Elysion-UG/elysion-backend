package com.elysion.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class PasswordService {

    private static final int SALT_LENGTH = 16; // Bytes
    private static final int BCRYPT_ROUNDS = 12;

    private final String pepper;

    public PasswordService() {
        this.pepper = System.getenv("PEPPER");
        if (this.pepper == null || this.pepper.isEmpty()) {
            throw new IllegalStateException("PEPPER environment variable not set");
        }
    }

    public String generateSalt() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public String hashPassword(String password, String salt) {
        String combined = password + salt + pepper;
        return BCrypt.hashpw(combined, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public boolean verifyPassword(String rawPassword, String salt, String storedHash) {
        String combined = rawPassword + salt + pepper;
        return BCrypt.checkpw(combined, storedHash);
    }
}