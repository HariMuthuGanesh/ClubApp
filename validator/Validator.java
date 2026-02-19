
import java.util.regex.Pattern;

public class Validator {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    public static boolean isValidEmail(String email) {
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        // At least 8 characters, one digit, one lower, one upper, one special char, no
        // whitespace
        return Pattern.compile(PASSWORD_REGEX).matcher(password).matches();
    }
}
