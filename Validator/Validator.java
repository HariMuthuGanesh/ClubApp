package validator;

public class Validator {

    public static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPassword(String pwd) {
        return pwd.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$");
    }
}
