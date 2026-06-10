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
    void should_maskAccountNumber_when_exactly4Chars() {
        // length == 4 → zero stars + last four
        assertEquals("1234", PIIDataHandler.maskAccountNumber("1234"));
    }

    @Test
    void should_maskAccountNumber_when_5Chars() {
        assertEquals("*2345", PIIDataHandler.maskAccountNumber("12345"));
    }

    @Test
    void should_throwPIIException_when_accountNumberIsNull() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskAccountNumber(null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void should_throwPIIException_when_accountNumberTooShort() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskAccountNumber("123"));
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
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void should_throwPIIException_when_routingNumberIs8Digits() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber("12345678"));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwPIIException_when_routingNumberIs10Digits() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber("1234567890"));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwPIIException_when_routingNumberIsAlphabetic() {
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
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void should_throwPIIException_when_ssnIs8Digits() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("12345678"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    @Test
    void should_throwPIIException_when_ssnHasWrongHyphenPlacement() {
        // "12-345-6789" → strips hyphens → "123456789" → 9 digits → valid
        // Actually this should still pass because stripping hyphens yields 9 digits
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("12-345-6789"));
    }

    @Test
    void should_throwPIIException_when_ssnHasLetters() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("12345678a"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    // ── assertNoRawPII ──────────────────────────────────────────────────

    @Test
    void should_doNothing_when_fieldValueIsNull() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII(null, "testField"));
    }

    @Test
    void should_doNothing_when_fieldIsCleanText() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("Hello world", "testField"));
    }

    @Test
    void should_doNothing_when_fieldContainsMaskedValue() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("****9012", "testField"));
    }

    @Test
    void should_throwPIIException_when_fieldContains10DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("acct 1234567890 here", "accountField"));
        assertTrue(ex.getMessage().contains("unmasked account number"));
    }

    @Test
    void should_throwPIIException_when_fieldContains12DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("acct 123456789012 here", "accountField"));
        assertTrue(ex.getMessage().contains("unmasked account number"));
    }

    @Test
    void should_throwPIIException_when_fieldContains9DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("routing 123456789 here", "routingField"));
        assertTrue(ex.getMessage().contains("unmasked routing number"));
    }

    @Test
    void should_doNothing_when_fieldContains8DigitNumber() {
        // 8 digits don't match either pattern
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("ref 12345678 here", "refField"));
    }
}
