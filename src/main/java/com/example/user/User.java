package com.example.user;

import java.util.UUID;

/**
 * Represents a User in the system.
 * Stores encrypted email and deterministic HMAC for lookup.
 */
public class User {
    private final String id;
    private final String name;
    private final String encryptedEmail;
    private final String emailHmac;

    public User(String name, String encryptedEmail, String emailHmac) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.encryptedEmail = encryptedEmail;
        this.emailHmac = emailHmac;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEncryptedEmail() {
        return encryptedEmail;
    }

    public String getEmailHmac() {
        return emailHmac;
    }

    /**
     * Decrypts the stored email. Callers should avoid printing the raw value.
     */
    public String getDecryptedEmail() throws Exception {
        return EmailSecurityUtil.decrypt(encryptedEmail);
    }

    /**
     * Returns a masked version of the email safe for logs: e.g. j***@example.com
     */
    public String maskedEmail() {
        try {
            String e = getDecryptedEmail();
            if (e == null) return "***@***";
            int at = e.indexOf('@');
            if (at <= 1) return "***@***";
            String local = e.substring(0, at);
            String domain = e.substring(at + 1);
            String visible = local.substring(0, 1);
            return visible + "***@" + domain;
        } catch (Exception ex) {
            return "***@***";
        }
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', email='" + maskedEmail() + "'}";
    }
}
