package Manage;

public class User extends Person {
    public static int userCount = 0;

    public User(String name, String email, String password) {
        super(name, email, password);
        userCount++;
    }
}