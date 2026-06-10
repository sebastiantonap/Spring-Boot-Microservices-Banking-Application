package org.training.transactions.service;

import org.junit.jupiter.api.Test;
import org.training.transactions.service.PIIDataHandler.PIIException;

import static org.junit.jupiter.api.Assertions.*;

class PIIDataHandlerTest {

    // ===== maskAccountNumber =====

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
        assertTrue(ex.getMessage().contains("too short to mask"));
    }

    @Test
    void should_maskAccountNumber_when_exactlyFourChars() {
        assertEquals("1234", PIIDataHandler.maskAccountNumber("1234"));
    }

    @Test
    void should_maskAccountNumber_when_twelveDigits() {
        assertEquals("********9012", PIIDataHandler.maskAccountNumber("123456789012"));
    }

    @Test
    void should_maskAccountNumber_when_tenDigits() {
        assertEquals("******7890", PIIDataHandler.maskAccountNumber("1234567890"));
    }

    // ===== maskRoutingNumber =====

    @Test
    void should_throwPIIException_when_routingNumberIsNull() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber(null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void should_throwPIIException_when_routingNumberHas8Digits() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskRoutingNumber("12345678"));
        assertTrue(ex.getMessage().contains("exactly 9 digits"));
    }

    @Test
    void should_throwPIIException_when_routingNumberHas10Digits() {
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

    @Test
    void should_maskRoutingNumber_when_valid9Digits() {
        assertEquals("*****6789", PIIDataHandler.maskRoutingNumber("123456789"));
    }

    // ===== maskSSN =====

    @Test
    void should_throwPIIException_when_ssnIsNull() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN(null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void should_throwPIIException_when_ssnHas8Digits() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("12345678"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    @Test
    void should_maskSSN_when_hyphensInNonStandardPositions() {
        // "12-345-6789" → strip hyphens → "123456789" → 9 digits → valid
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("12-345-6789"));
    }

    @Test
    void should_throwPIIException_when_ssnTooFewDigitsAfterStrippingHyphens() {
        // "1-2-345678" → strip hyphens → "12345678" → 8 digits → invalid
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("1-2-345678"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    @Test
    void should_maskSSN_when_nineDigitsNoHyphens() {
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("123456789"));
    }

    @Test
    void should_maskSSN_when_standardHyphenatedFormat() {
        assertEquals("***-**-6789", PIIDataHandler.maskSSN("123-45-6789"));
    }

    @Test
    void should_throwPIIException_when_ssnHasLetters() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.maskSSN("12345abcd"));
        assertTrue(ex.getMessage().contains("9 digits"));
    }

    // ===== assertNoRawPII =====

    @Test
    void should_notThrow_when_fieldValueIsNull() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII(null, "field"));
    }

    @Test
    void should_notThrow_when_fieldValueIsClean() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("Hello World", "notes"));
    }

    @Test
    void should_notThrow_when_fieldContainsMaskedValue() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("****9012", "account"));
    }

    @Test
    void should_throwPIIException_when_fieldContains10DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("Account is 1234567890 here", "notes"));
        assertTrue(ex.getMessage().contains("unmasked account number"));
    }

    @Test
    void should_throwPIIException_when_fieldContains12DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("Acct 123456789012", "field"));
        assertTrue(ex.getMessage().contains("unmasked account number"));
    }

    @Test
    void should_throwPIIException_when_fieldContains9DigitNumber() {
        PIIException ex = assertThrows(PIIException.class,
                () -> PIIDataHandler.assertNoRawPII("Routing 123456789 found", "field"));
        assertTrue(ex.getMessage().contains("unmasked routing number or SSN"));
    }

    @Test
    void should_notThrow_when_fieldContains8DigitNumber() {
        assertDoesNotThrow(() -> PIIDataHandler.assertNoRawPII("Code 12345678 ok", "field"));
    }
}
