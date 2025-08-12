package com.elysion.tools;

import com.elysion.security.PasswordService;

public class HashTool {

    public static void main(String[] args) {
        // Passwort per -Dpasswd=... reinreichen
        String password = System.getProperty("passwd");
        if (password == null || password.isBlank()) {
            System.err.println("Fehler: Bitte mit -Dpasswd=<PASSWORD> starten.");
            System.exit(1);
        }

        // Optional: vorhandenes Salt per -Dsalt=... (Base64) vorgeben
        String providedSalt = System.getProperty("salt");

        try {
            // Nutzt 1:1 deinen PasswordService (inkl. PEPPER aus Env, BCrypt Runden, Base64 usw.)
            PasswordService ps = new PasswordService();

            String salt = (providedSalt != null && !providedSalt.isBlank())
                    ? providedSalt
                    : ps.generateSalt();

            String hash = ps.hashPassword(password, salt);

            // Ausgabe für Liquibase-Insert
            System.out.println("SALT=" + salt);
            System.out.println("HASH=" + hash);

            // Mini-Selfcheck
            boolean ok = ps.verifyPassword(password, salt, hash);
            System.out.println("VERIFY=" + ok);
        } catch (IllegalStateException e) {
            System.err.println("Fehler: " + e.getMessage() + " (PEPPER setzen!)");
            System.exit(2);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(3);
        }
    }
}