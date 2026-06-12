package org.training.transactions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.training.transactions.service.AuthService.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    // ── validateSSOToken ────────────────────────────────────────────────

    @Test
    void should_returnTrue_when_tokenIsValid() {
        String token = "Bearer header.payload.signature";
        assertTrue(authService.validateSSOToken(token));
    }

    @Test
    void should_throwAuthenticationException_when_tokenIsNull() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken(null));
        assertTrue(ex.getMessage().contains("null or empty"));
    }

    @Test
    void should_throwAuthenticationException_when_tokenIsBlank() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken("   "));
        assertTrue(ex.getMessage().contains("null or empty"));
    }

    @Test
    void should_throwAuthenticationException_when_tokenMissingBearerPrefix() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken("header.payload.signature"));
        assertTrue(ex.getMessage().contains("missing Bearer prefix"));
    }

    @Test
    void should_throwAuthenticationException_when_tokenHasWrongSegmentCount() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken("Bearer a.b"));
        assertTrue(ex.getMessage().contains("malformed"));
    }

    @Test
    void should_throwAuthenticationException_when_tokenHasTwoSegments() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken("Bearer only.two"));
        assertTrue(ex.getMessage().contains("malformed"));
    }

    @Test
    void should_throwAuthenticationException_when_tokenHasFourSegments() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken("Bearer a.b.c.d"));
        assertTrue(ex.getMessage().contains("malformed"));
    }

    @Test
    void should_throwAuthenticationException_when_tokenHasNoSegments() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateSSOToken("Bearer nodots"));
        assertTrue(ex.getMessage().contains("malformed"));
    }

    // ── validateMFA ─────────────────────────────────────────────────────

    @Test
    void should_returnTrue_when_mfaCodeIsValid() {
        assertTrue(authService.validateMFA("user1", "123456"));
    }

    @Test
    void should_returnFalse_when_mfaCodeIsWrong() {
        assertFalse(authService.validateMFA("user1", "654321"));
    }

    @Test
    void should_throwAuthenticationException_when_userIdIsNull() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA(null, "123456"));
        assertTrue(ex.getMessage().contains("User ID must not be null"));
    }

    @Test
    void should_throwAuthenticationException_when_userIdIsBlank() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA("  ", "123456"));
        assertTrue(ex.getMessage().contains("User ID must not be null"));
    }

    @Test
    void should_throwAuthenticationException_when_mfaCodeIsNull() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA("user1", null));
        assertTrue(ex.getMessage().contains("MFA code must not be null"));
    }

    @Test
    void should_throwAuthenticationException_when_mfaCodeIsBlank() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA("user1", "   "));
        assertTrue(ex.getMessage().contains("MFA code must not be null"));
    }

    @Test
    void should_throwAuthenticationException_when_mfaCodeIsNonNumeric() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA("user1", "abcdef"));
        assertTrue(ex.getMessage().contains("6-digit numeric"));
    }

    @Test
    void should_throwAuthenticationException_when_mfaCodeHasFiveDigits() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA("user1", "12345"));
        assertTrue(ex.getMessage().contains("6-digit numeric"));
    }

    @Test
    void should_throwAuthenticationException_when_mfaCodeHasSevenDigits() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.validateMFA("user1", "1234567"));
        assertTrue(ex.getMessage().contains("6-digit numeric"));
    }

    // ── isSessionActive ─────────────────────────────────────────────────

    @Test
    void should_returnTrue_when_sessionIdIs32Chars() {
        String sessionId = "a".repeat(32);
        assertTrue(authService.isSessionActive(sessionId));
    }

    @Test
    void should_returnFalse_when_sessionIdIsNull() {
        assertFalse(authService.isSessionActive(null));
    }

    @Test
    void should_returnFalse_when_sessionIdIsBlank() {
        assertFalse(authService.isSessionActive("   "));
    }

    @Test
    void should_returnFalse_when_sessionIdIs31Chars() {
        String sessionId = "a".repeat(31);
        assertFalse(authService.isSessionActive(sessionId));
    }

    @Test
    void should_returnFalse_when_sessionIdIs33Chars() {
        String sessionId = "a".repeat(33);
        assertFalse(authService.isSessionActive(sessionId));
    }
}
