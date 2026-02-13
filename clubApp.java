import java.util.*;
import Clubs.*;
import Manage.*;
import validator.Validator;
import Details.getAdminDetails;

public class clubApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Admin");
            System.out.println("2. Register");
            System.out.println("3. Coordinator Login (TBD)");
            System.out.println("4. Student Login (TBD)");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    adminLogin(sc);
                    break;
                case 2:
                    registerUser(sc);
                    break;
                case 3:
                case 4:
                    System.out.println("Logic for this role is in progress.");
                    break;
                case 5:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void adminLogin(Scanner sc) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add new event to club");
            System.out.println("2. Remove club (TBD)");
            System.out.println("3. View all clubs / list events");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    ClubAll.addEvent(sc);
                    break;
                case 2:
                    System.out.println("Removing club logic TBD.");
                    break;
                case 3:
                    ClubAll.viewAllClubs(sc);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void registerUser(Scanner sc) {
        while (true) {
            System.out.println("\n--- Registration Menu ---");
            System.out.println("1. Register as Admin");
            System.out.println("2. Register as Coordinator");
            System.out.println("3. Register as Student");
            System.out.println("4. Back to main menu");
            System.out.print("Enter your choice: ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    getAdminDetails.getAdminDetails(sc);
                    break;
                case 2:
                    getCoordinatorDetails(sc);
                    break;
                case 3:
                    getStudentDetails(sc);
                    break;
                case 4:
                    System.out.println("Returning to main menu...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void getStudentDetails(Scanner sc) {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (!Validator.isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            System.out.println("Invalid password format.");
            return;
        }
        new User(name, email, password);
        System.out.println("Student registered successfully!");
    }

    public static void getCoordinatorDetails(Scanner sc) {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (!Validator.isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            System.out.println("Invalid password format.");
            return;
        }
        new User(name, email, password);
        System.out.println("Coordinator registered successfully!");
    }
}
