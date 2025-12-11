package com.example.user;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for encrypting/decrypting emails and producing deterministic HMACs for lookup.
 * Relies on environment variable SECRET_KEY being set to a server-only secret.
 */
public final class EmailSecurityUtil {
    private static final String SECRET_ENV = "SECRET_KEY";
    private static final byte[] AES_KEY;
    private static final byte[] HMAC_KEY;
    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getEncoder();
    private static final Base64.Decoder B64D = Base64.getDecoder();

    static {
        String secret = System.getenv(SECRET_ENV);
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("Missing environment variable: " + SECRET_ENV);
        }
        try {
            byte[] master = secret.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            AES_KEY = sha.digest(concat(master, "AES_KEY".getBytes("UTF-8")));
            HMAC_KEY = sha.digest(concat(master, "HMAC_KEY".getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EmailSecurityUtil", e);
        }
    }

    private EmailSecurityUtil() {}

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    /**
     * Encrypt plaintext using AES-GCM (12-byte IV). Returns base64(iv|ciphertext).
     */
    public static String encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[12];
        RNG.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(AES_KEY, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] ct = cipher.doFinal(plaintext.getBytes("UTF-8"));
        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        return B64.encodeToString(out);
    }

    /**
     * Decrypts base64(iv|ciphertext) produced by encrypt().
     */
    public static String decrypt(String blob) throws Exception {
        byte[] all = B64D.decode(blob);
        if (all.length < 13) return null;
        byte[] iv = new byte[12];
        System.arraycopy(all, 0, iv, 0, iv.length);
        byte[] ct = new byte[all.length - iv.length];
        System.arraycopy(all, iv.length, ct, 0, ct.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(AES_KEY, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, "UTF-8");
    }

    /**
     * Deterministic HMAC-SHA256 hex for normalized email (used for lookups and rate-limiting keys).
     */
    public static String hmacHex(String input) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(HMAC_KEY, "HmacSHA256");
        mac.init(key);
        byte[] out = mac.doFinal(input.getBytes("UTF-8"));
        return bytesToHex(out);
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(2 * b.length);
        for (byte x : b) {
            sb.append(String.format("%02x", x & 0xff));
        }
        return sb.toString();
    }
}
