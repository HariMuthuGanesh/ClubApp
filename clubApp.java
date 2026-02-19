import java.util.Scanner;

public class clubApp {
    private static UserManager userManager = new UserManager();
    private static ClubManager clubManager = new ClubManager();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        // Pre-register an admin for testing convenience
        userManager.addAdmin(new Admin("HMG", "hmg@club.com", "Hmg@123"));

        while (true) {
            System.out.println("\n========================================================");
            System.out.println("   Welcome to ClubApp – Dynamic Club Management System");
            System.out.println("========================================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegistration();
                    break;
                case "3":
                    System.out.println("Thank you for using ClubApp!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- Login ---");
        System.out.println("1. Admin");
        System.out.println("2. Coordinator");
        System.out.println("3. Student");
        System.out.print("Select Role: ");
        String role = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        switch (role) {
            case "1":
                Admin admin = userManager.validateAdmin(email, password);
                if (admin != null)
                    adminMenu(admin);
                else
                    System.out.println("Invalid credentials!");
                break;
            case "2":
                User coord = userManager.validateCoordinator(email, password);
                if (coord != null)
                    coordinatorMenu(coord);
                else
                    System.out.println("Invalid credentials!");
                break;
            case "3":
                User student = userManager.validateStudent(email, password);
                if (student != null)
                    studentMenu(student);
                else
                    System.out.println("Invalid credentials!");
                break;
            default:
                System.out.println("Invalid role!");
        }
    }

    private static void handleRegistration() {
        System.out.println("\n--- Register ---");
        System.out.println("1. Admin");
        System.out.println("2. Coordinator");
        System.out.println("3. Student");
        System.out.print("Select Role: ");
        String role = sc.nextLine();

        switch (role) {
            case "1":
                AdminDetails.register(userManager);
                break;
            case "2":
                CoordinatorDetails.register(userManager);
                break;
            case "3":
                StudentDetails.register(userManager);
                break;
            default:
                System.out.println("Invalid role!");
        }
    }

    private static void adminMenu(Admin admin) {
        while (true) {
            System.out.println("\n--- Admin Menu (Welcome " + admin.getName() + ") ---");
            System.out.println("1. Create New Club");
            System.out.println("2. View All Clubs");
            System.out.println("3. Logout");
            System.out.print("Select Choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                System.out.print("Enter Club Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Description: ");
                String desc = sc.nextLine();
                System.out.print("Enter Coordinator Email: ");
                String email = sc.nextLine();

                User coordinator = userManager.findCoordinatorByEmail(email);
                if (coordinator != null) {
                    Club newClub = new Club(name, desc, coordinator);
                    clubManager.addClub(newClub);
                    System.out.println("Club created successfully!");
                } else {
                    System.out.println("Coordinator not found! Register them first.");
                }
            } else if (choice.equals("2")) {
                clubManager.listAllClubs();
            } else if (choice.equals("3")) {
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
    }

    private static void coordinatorMenu(User coordinator) {
        Club club = clubManager.getClubByCoordinator(coordinator);
        if (club == null) {
            System.out.println("You are not assigned to any club yet. Contact Admin.");
            return;
        }

        while (true) {
            System.out.println("\n--- Coordinator Menu: " + club.getClubName() + " ---");
            System.out.println("1. View Club Details");
            System.out.println("2. Add Event");
            System.out.println("3. View Events");
            System.out.println("4. Remove Event");
            System.out.println("5. Update Event");
            System.out.println("6. Logout");
            System.out.print("Select Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    club.viewDetails();
                    break;
                case "2":
                    System.out.print("Event Name: ");
                    String ename = sc.nextLine();
                    System.out.print("Description: ");
                    String edesc = sc.nextLine();
                    System.out.print("Venue: ");
                    String venue = sc.nextLine();
                    System.out.print("Date: ");
                    String date = sc.nextLine();
                    club.addEvent(new Event(ename, edesc, venue, date));
                    break;
                case "3":
                    club.listEvents();
                    break;
                case "4":
                    club.listEvents();
                    System.out.print("Enter event number to remove: ");
                    try {
                        int idx = Integer.parseInt(sc.nextLine()) - 1;
                        club.removeEvent(idx);
                    } catch (Exception e) {
                        System.out.println("Invalid input!");
                    }
                    break;
                case "5":
                    club.listEvents();
                    System.out.print("Enter event number to update: ");
                    try {
                        int index = Integer.parseInt(sc.nextLine()) - 1;
                        System.out.print("New Name: ");
                        String nname = sc.nextLine();
                        System.out.print("New Description: ");
                        String ndesc = sc.nextLine();
                        System.out.print("New Venue: ");
                        String nvenue = sc.nextLine();
                        System.out.print("New Date: ");
                        String ndate = sc.nextLine();
                        club.updateEvent(index, new Event(nname, ndesc, nvenue, ndate));
                    } catch (Exception e) {
                        System.out.println("Invalid input!");
                    }
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void studentMenu(User student) {
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. View All Clubs");
            System.out.println("2. Select Club to View Events");
            System.out.println("3. Logout");
            System.out.print("Select Choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                clubManager.listAllClubs();
            } else if (choice.equals("2")) {
                clubManager.listAllClubs();
                if (clubManager.getClubCount() > 0) {
                    System.out.print("Enter club number: ");
                    try {
                        int idx = Integer.parseInt(sc.nextLine()) - 1;
                        Club selected = clubManager.getClubByIndex(idx);
                        if (selected != null) {
                            selected.viewDetails();
                            selected.listEvents();
                        } else {
                            System.out.println("Invalid club number!");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid input!");
                    }
                }
            } else if (choice.equals("3")) {
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
    }
}
