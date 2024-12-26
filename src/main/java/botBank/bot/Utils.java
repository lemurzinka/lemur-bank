package botBank.bot;

import org.apache.commons.validator.EmailValidator;

public class Utils {
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }}
