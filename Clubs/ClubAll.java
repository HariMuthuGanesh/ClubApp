package Clubs;

import java.util.Scanner;

public class ClubAll {
    private static Club csi = new CsiClub();
    private static Club ieee = new Ieee();
    private static Club cse = new CseAsso();

    public static void addEvent(Scanner sc) {
        while (true) {
            System.out.println("1. CSI");
            System.out.println("2. IEEE");
            System.out.println("3. CSE ASSOCIATION");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    csi.addEvent(sc);
                    break;
                case 2:
                    ieee.addEvent(sc);
                    break;
                case 3:
                    cse.addEvent(sc);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void viewAllClubs(Scanner sc) {
        while (true) {
            System.out.println("1. CSI");
            System.out.println("2. IEEE");
            System.out.println("3. CSE ASSOCIATION");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    csi.viewDetails();
                    csi.listEvents();
                    break;
                case 2:
                    ieee.viewDetails();
                    ieee.listEvents();
                    break;
                case 3:
                    cse.viewDetails();
                    cse.listEvents();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}