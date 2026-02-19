
import java.util.Scanner;

public class StudentDetails {
    public static void register(UserManager userManager) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n--- Student Registration ---");
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = sc.nextLine();
            if (Validator.isValidEmail(email)) {
                if (userManager.findStudentByEmail(email) == null)
                    break;
                else
                    System.out.println("Email already registered!");
            } else {
                System.out.println("Invalid Email format! Try again.");
            }
        }

        String password;
        while (true) {
            System.out.print("Enter Password: ");
            password = sc.nextLine();
            if (Validator.isValidPassword(password))
                break;
            System.out.println("Invalid Password!");
        }

        User student = new User(name, email, password);
        userManager.addStudent(student);
        System.out.println("Student registered successfully!");
    }
}
