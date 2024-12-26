package botBank.bot;

import org.apache.commons.validator.EmailValidator;

import java.util.regex.Pattern;

public class Utils {


    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?\\d{10,15}$");


    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);

    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }

}
