package bot_bank.service;

import org.apache.commons.validator.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * ValidationService provides methods for validating and encoding user input, such as phone numbers,
 * passwords, and emails. It ensures that input meets specific criteria and securely encodes passwords.
 */

@Service
public class ValidationService {

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?\\d{10,15}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\\$%^&*])(?!.*\\s).{6,}$"
    );  // One uppercase, one lowercase letter, one digit, one special character, no spaces

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }
}
