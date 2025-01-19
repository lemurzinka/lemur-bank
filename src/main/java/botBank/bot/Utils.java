package botBank.bot;

import org.apache.commons.validator.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

public class Utils {


    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?\\d{10,15}$");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\\$%\\^&\\*])(?=\\S+$).{6,11}$");  //One big&&small letter, one digit, one special symbol, no spaces


    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public static String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

        public static boolean isValidPassword(String password) {
            if (password == null) {
                return false;
            }
            return PASSWORD_PATTERN.matcher(password).matches();
        }


        public static boolean matchesPassword(String rawPassword, String encodedPassword) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }


    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);

    }


    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }


}
