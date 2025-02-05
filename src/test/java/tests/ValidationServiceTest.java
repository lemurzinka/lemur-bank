package tests;

import bot_bank.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ValidationServiceTest contains unit tests for the ValidationService class. It verifies the correctness
 * of methods related to validating and encoding user input, such as passwords, emails, and phone numbers.
 */

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void testEncodePassword() {
        String rawPassword = "Password123!";
        String encodedPassword = validationService.encodePassword(rawPassword);

        assertNotNull(encodedPassword);
        assertTrue(validationService.matchesPassword(rawPassword, encodedPassword));
    }

    @Test
    void testIsValidPassword() {
        assertTrue(validationService.isValidPassword("P@ssw0rs"));
        assertFalse(validationService.isValidPassword("password123!")); // missing uppercase letter
        assertFalse(validationService.isValidPassword("PASSWORD123!")); // missing lowercase letter
        assertFalse(validationService.isValidPassword("Password!"));    // missing digit
        assertFalse(validationService.isValidPassword("Password123"));  // missing special character
        assertFalse(validationService.isValidPassword("Pwd1!"));        // too short
        assertFalse(validationService.isValidPassword(null)); // null password
    }

    @Test
    void testMatchesPassword() {
        String rawPassword = "Password123!";
        String encodedPassword = validationService.encodePassword(rawPassword);

        assertTrue(validationService.matchesPassword(rawPassword, encodedPassword));
        assertFalse(validationService.matchesPassword("WrongPassword123!", encodedPassword));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(validationService.isValidEmail("test@example.com"));
        assertFalse(validationService.isValidEmail("invalid-email"));
        assertFalse(validationService.isValidEmail("test@.com"));
        assertFalse(validationService.isValidEmail("test@com"));
        assertFalse(validationService.isValidEmail("test@com."));
        assertFalse(validationService.isValidEmail(null)); // null email
    }

    @Test
    void testIsValidPhoneNumber() {
        assertTrue(validationService.isValidPhoneNumber("+1234567890"));
        assertTrue(validationService.isValidPhoneNumber("1234567890"));
        assertFalse(validationService.isValidPhoneNumber("12345")); // too short
        assertFalse(validationService.isValidPhoneNumber("1234567890123456")); // too long
        assertFalse(validationService.isValidPhoneNumber("123-456-7890")); // invalid characters
        assertFalse(validationService.isValidPhoneNumber(null)); // null phone number
    }
}
