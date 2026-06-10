package org.training.transactions.service;

import org.springframework.stereotype.Service;
import java.time.Instant;

/**
 * AuthService - Handles SSO/MFA token validation for transaction authorization.
 * OCC compliance path: All transaction requests must be authenticated and logged.
 */
@Service
public class AuthService {

    private static final long TOKEN_EXPIRY_SECONDS = 3600;

    /**
     * Validates the SSO token provided in the transaction request.
     * OCC compliance path — must have test coverage.
     *
     * @param token JWT token from the request header
     * @return true if token is valid and not expired
     * @throws AuthenticationException if token is null, malformed, or expired
     */
    public boolean validateSSOToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("SSO token must not be null or empty");
        }
        if (!token.startsWith("Bearer ")) {
            throw new AuthenticationException("SSO token format invalid: missing Bearer prefix");
        }
        String rawToken = token.substring(7);
        if (rawToken.split("\\.").length != 3) {
            throw new AuthenticationException("SSO token is malformed");
        }
        if (isTokenExpired(rawToken)) {
            throw new AuthenticationException("SSO token has expired");
        }
        return true;
    }

    /**
     * Validates MFA challenge response for high-value transactions.
     * OCC compliance path — MFA required for transactions above $10,000.
     *
     * @param userId   the user initiating the transaction
     * @param mfaCode  the one-time passcode from the MFA device
     * @return true if MFA code is valid
     * @throws AuthenticationException if MFA code is null, expired, or incorrect
     */
    public boolean validateMFA(String userId, String mfaCode) {
        if (userId == null || userId.isBlank()) {
            throw new AuthenticationException("User ID must not be null for MFA validation");
        }
        if (mfaCode == null || mfaCode.isBlank()) {
            throw new AuthenticationException("MFA code must not be null or empty");
        }
        if (!mfaCode.matches("\\d{6}")) {
            throw new AuthenticationException("MFA code must be a 6-digit numeric code");
        }
        // Simulated validation — in production this delegates to LDAP/SSO provider
        return simulateMFAValidation(userId, mfaCode);
    }

    /**
     * Checks whether a given user session is still active.
     * OCC compliance path — inactive sessions must not authorize transactions.
     *
     * @param sessionId the session identifier
     * @return true if the session is active
     */
    public boolean isSessionActive(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        // Simulated session store — in production backed by Redis or LDAP
        return sessionId.length() == 32;
    }

    // -- Private helpers --

    private boolean isTokenExpired(String rawToken) {
        // Simulated expiry check based on token issuance timestamp
        long issuedAt = extractIssuedAt(rawToken);
        return Instant.now().getEpochSecond() - issuedAt > TOKEN_EXPIRY_SECONDS;
    }

    private long extractIssuedAt(String rawToken) {
        // Simplified: in production this decodes the JWT payload
        return Instant.now().getEpochSecond() - 100;
    }

    private boolean simulateMFAValidation(String userId, String mfaCode) {
        // Simplified: in production delegates to LDAP or MFA provider
        return mfaCode.equals("123456");
    }

    // -- Exception class --

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
