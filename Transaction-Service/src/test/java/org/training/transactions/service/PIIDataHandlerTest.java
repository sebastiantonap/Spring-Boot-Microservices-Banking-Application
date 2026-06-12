package org.training.transactions.service;

import org.junit.jupiter.api.Test;
import org.training.transactions.service.PIIDataHandler.PIIException;

import static org.junit.jupiter.api.Assertions.*;

class PIIDataHandlerTest {

    // ── maskAccountNumber ───────────────────────────────────────────────

    @Test
    void should_maskAccountNumber_when_12Digits() {
        assertEquals("********9012", PIIDataHandler.maskAccountNumber("123456789012"));
    }

    @Test
    void should_maskAccountNumber_when_4Chars() {
        assertEquals("9012", PIIDataHandler.maskAccountNumber("9012"));
    }

    @Test
    void should_maskAccountNumber_when_10Digits() {
        assertEquals("******7890", PIIDataHandler.maskAccountNumber("1234567890"));
    }

    @Test
    void should_throwPIIException_when_accountNumberIsNull() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskAccountNumber(null));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void should_throwPIIException_when_accountNumberTooShort() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskAccountNumber("abc"));
        assertTrue(ex.getMessage().contains("too short"));
    }

    // ── maskRoutingNumber ───────────────────────────────────────────────

    @Test
    void should_maskRoutingNumber_when_valid9Digits() {
        assertEquals("*****6789", PIIDataHandler.maskRoutingNumber("123456789"));
    }

    @Test
    void should_throwPIIException_when_routingNumberIsNull() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber(null));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void should_throwPIIException_when_routingNumberTooShort() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber("12345678"));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwPIIException_when_routingNumberTooLong() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber("1234567890"));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwPIIException_when_routingNumberAlphabetic() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber("abcdefghi"));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    // ── maskSSN ─────────────────────────────────────────────────────────

    @Test
    void should_maskSSN_when_9DigitsNoHyphens() {
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("123456789"));
    }

    @Test
    void should_maskSSN_when_formattedWithHyphens() {
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("123-45-6789"));
    }

    @Test
    void should_throwPIIException_when_ssnIsNull() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN(null));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void should_throwPIIException_when_ssnTooShort() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("12345678"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    @Test
    void should_maskSSN_when_hyphensInNonStandardPositions() {
        // replaceAll("-","") yields "123456789" (9 digits) — still valid
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("12-345-6789"));
    }

    @Test
    void should_throwPIIException_when_ssnContainsLetters() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("12345678a"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    // ── assertNoRawPII ──────────────────────────────────────────────────

    @Test
    void should_pass_when_fieldValueIsNull() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII(null, "notes"));
    }

    @Test
    void should_pass_when_fieldValueIsClean() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("safe text here", "notes"));
    }

    @Test
    void should_pass_when_fieldContainsMaskedValue() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("****9012", "account"));
    }

    @Test
    void should_throwPIIException_when_fieldContains10DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("acct 1234567890 info", "notes"));
        assertTrue(ex.getMessage().contains("unmasked account number"));
    }

    @Test
    void should_throwPIIException_when_fieldContains12DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("acct 123456789012 info", "notes"));
        assertTrue(ex.getMessage().contains("unmasked account number"));
    }

    @Test
    void should_throwPIIException_when_fieldContains9DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("routing 123456789 info", "notes"));
        assertTrue(ex.getMessage().contains("unmasked routing number"));
    }
}
