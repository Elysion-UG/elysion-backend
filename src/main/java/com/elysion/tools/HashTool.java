package com.elysion.tools;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

public class HashTool {
    public static void main(String[] args) {
        String password = System.getProperty("passwd"); // -Dpasswd=DeinPasswort
        if (password == null || password.isBlank()) {
            System.err.println("Bitte gib -Dpasswd=<PASSWORD> mit.");
            System.exit(1);
        }
        String pepper = System.getenv("PEPPER");
        if (pepper == null || pepper.isBlank()) {
            System.err.println("Bitte exportiere PEPPER in der Shell.");
            System.exit(2);
        }

        // eigenes Salt (separat gespeichert)
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);

        // kombiniere wie in deinem PasswordService (wichtig: gleiche Reihenfolge!)
        String combined = pepper + password + salt;

        // bcrypt mit z.B. 12 Rounds (wie in deinem Service)
        String bcrypt = BCrypt.hashpw(combined, BCrypt.gensalt(12));

        System.out.println("SALT=" + salt);
        System.out.println("HASH=" + bcrypt);
    }
}