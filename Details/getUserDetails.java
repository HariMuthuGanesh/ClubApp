package Details;

import java.util.Scanner;
import validator.Validator;
import Manage.User;

public class getUserDetails {
    public static void getUserDetails(Scanner sc) {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (!Validator.isValidEmail(email)) {
            System.out.println("Invalid email format. Please try again.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            System.out.println(
                    "Invalid password format. Password must be at least 6 characters long and contain at least one uppercase letter, one lowercase letter, and one digit.");
            return;
        }
        User user = new User(name, email, password);
        System.out.println("User registered successfully!");
    }
}