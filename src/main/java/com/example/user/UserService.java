package com.example.user;

import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.Locale;
import java.time.Instant;

/**
 * Service for managing users in the system with MITM attack protections.
 */
public class UserService {
    // In-memory store keyed by deterministic email HMAC
    private final Map<String, User> store = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> createTimestamps = new ConcurrentHashMap<>();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final int MAX_NAME_LENGTH = 256;
    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321
    private static final int MAX_CREATES_PER_MIN = 10;
    private static final long WINDOW_MS = 60_000L; // 1 minute

    public UserService() {
        // no-op
    }

    /**
     * Create a new user in the system with security hardening.
     */
    public User createUser(String name, String email) throws ValidationException {
        try {
            // Basic structure checks
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Name is required and cannot be empty");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email is required and cannot be empty");
            }

            String normEmail = email.trim().toLowerCase(Locale.ROOT);

            // Length checks
            if (name.length() > MAX_NAME_LENGTH) {
                throw new ValidationException("Name exceeds maximum length of " + MAX_NAME_LENGTH);
            }
            if (normEmail.length() > MAX_EMAIL_LENGTH) {
                throw new ValidationException("Email exceeds maximum length of " + MAX_EMAIL_LENGTH);
            }

            // Format validation
            if (!EMAIL_PATTERN.matcher(normEmail).matches()) {
                throw new ValidationException("Email format is invalid");
            }

            // Deterministic identity token (HMAC) for dedup and rate limiting
            String emailHmac = EmailSecurityUtil.hmacHex(normEmail);

            // Rate limiting per identity
            rateLimitCheck(emailHmac);

            // Duplicate check
            if (store.containsKey(emailHmac)) {
                throw new DuplicateEmailException("Email already exists in the system");
            }

            // Encrypt email for storage
            String encrypted = EmailSecurityUtil.encrypt(normEmail);

            User user = new User(name.trim(), encrypted, emailHmac);
            store.put(emailHmac, user);

            // Secure audit log
            logSecurityEvent("USER_CREATED", user.maskedEmail());

            return user;
        } catch (ValidationException ve) {
            throw ve;
        } catch (DuplicateEmailException de) {
            throw de;
        } catch (Exception e) {
            // Wrap crypto/other errors without leaking detail
            logSecurityEvent("INTERNAL_ERROR", "user_creation_failed");
            throw new ValidationException("Internal error processing request");
        }
    }

    private void rateLimitCheck(String emailHmac) throws ValidationException {
        long now = Instant.now().toEpochMilli();
        Deque<Long> dq = createTimestamps.computeIfAbsent(emailHmac, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && dq.peekFirst() <= now - WINDOW_MS) dq.pollFirst();
            if (dq.size() >= MAX_CREATES_PER_MIN) {
                logSecurityEvent("RATE_LIMIT_EXCEEDED", maskHmac(emailHmac));
                throw new ValidationException("Rate limit exceeded for this identity");
            }
            dq.addLast(now);
        }
    }

    private String maskHmac(String hmac) {
        if (hmac == null || hmac.length() < 6) return "***";
        return hmac.substring(0, 4) + "***" + hmac.substring(hmac.length() - 2);
    }

    private void logSecurityEvent(String eventType, String details) {
        String timestamp = Instant.now().toString();
        System.out.println("[SECURITY_LOG] " + timestamp + " | EVENT: " + eventType + " | DETAILS: " + details);
    }

    public int getUserCount() {
        return store.size();
    }

    /**
     * Lookup user by plaintext email (returns empty if not found or on error).
     */
    public java.util.Optional<User> findByEmail(String email) {
        try {
            if (email == null) return java.util.Optional.empty();
            String norm = email.trim().toLowerCase(Locale.ROOT);
            String hmac = EmailSecurityUtil.hmacHex(norm);
            return java.util.Optional.ofNullable(store.get(hmac));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }
}
