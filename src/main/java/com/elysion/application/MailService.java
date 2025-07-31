package com.elysion.application;

import com.elysion.domain.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MailService {

    public void sendActivationMail(User user) {
        String activationLink = "https://deine-domain.de/confirm?token=" + user.activationToken;
        System.out.println("👉 Bestätigungslink: " + activationLink);

        // Oder echtes E-Mail-System:
        // mailer.send(Mail.withText(user.email, "Bestätige dein Konto", "Link: " + activationLink));
    }
}