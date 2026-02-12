package Manage;

public class Admin extends Person {
    public static int adminCount = 0;

    public Admin(String name, String email, String password) {
        super(name, email, password);
        adminCount++;
    }
}