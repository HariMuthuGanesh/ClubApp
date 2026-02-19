
import java.util.Scanner;

public class AdminDetails {
    public static void register(UserManager userManager) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n--- Admin Registration ---");
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = sc.nextLine();
            if (Validator.isValidEmail(email))
                break;
            System.out.println("Invalid Email format! Try again.");
        }

        String password;
        while (true) {
            System.out.print("Enter Password (min 8 chars, 1 upper, 1 lower, 1 digit, 1 special): ");
            password = sc.nextLine();
            if (Validator.isValidPassword(password))
                break;
            System.out.println("Invalid Password! Must meet security requirements.");
        }

        Admin admin = new Admin(name, email, password);
        userManager.addAdmin(admin);
        System.out.println("Admin registered successfully!");
    }
}
