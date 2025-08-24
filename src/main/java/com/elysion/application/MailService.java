package com.elysion.application;

import com.elysion.domain.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MailService {

    public void sendActivationMail(User user) {
        String activationLink = "https://100.66.219.89:8080/users/confirm-email?token=" + user.activationToken;
        System.out.println("👉 Bestätigungslink: " + activationLink);

        // Oder echtes E-Mail-System:
        // mailer.send(Mail.withText(user.email, "Bestätige dein Konto", "Link: " + activationLink));
    }

    /**
     * Sendet einen Bestätigungslink an die neue, noch nicht aktive E-Mail-Adresse.
     * Der User muss diesen Link klicken, um die Änderung abzuschließen.
     */
    public void sendEmailChangeConfirmation(User user) {
        String confirmLink = "https://100.66.219.89:8080/users/confirm-email?token=" + user.activationToken;
        System.out.println("👉 Bestätigungslink für neue E-Mails: " + confirmLink);

        // Bei echtem Setup:
        // mailer.send(Mail.withText(
        //     user.pendingEmail,
        //     "Bitte bestätige deine neue E-Mail-Adresse",
        //     "Klicke hier zum Bestätigen:" + confirmLink
        // ));
    }
}