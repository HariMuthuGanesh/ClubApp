package manage;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class UserManager {
    private List<Admin> admins = new ArrayList<>();
    private List<User> coordinators = new ArrayList<>();
    private List<User> students = new ArrayList<>();
    private static final String FILE_PATH = "file/users.txt";

    public UserManager() {
        loadUsers();
    }

    public void addAdmin(Admin admin) {
        admins.add(admin);
        saveUsers();
    }

    public void addCoordinator(User coordinator) {
        coordinators.add(coordinator);
        saveUsers();
    }

    public void addStudent(User student) {
        students.add(student);
        saveUsers();
    }

    public Admin validateAdmin(String email, String password) {
        for (Admin admin : admins) {
            if (admin.getEmail().equalsIgnoreCase(email) && admin.getPassword().equals(password)) {
                return admin;
            }
        }
        return null;
    }

    public User validateCoordinator(String email, String password) {
        for (User coord : coordinators) {
            if (coord.getEmail().equalsIgnoreCase(email) && coord.getPassword().equals(password)) {
                return coord;
            }
        }
        return null;
    }

    public User validateStudent(String email, String password) {
        for (User student : students) {
            if (student.getEmail().equalsIgnoreCase(email) && student.getPassword().equals(password)) {
                return student;
            }
        }
        return null;
    }

    public User findCoordinatorByEmail(String email) {
        for (User coord : coordinators) {
            if (coord.getEmail().equalsIgnoreCase(email)) {
                return coord;
            }
        }
        return null;
    }

    public User findStudentByEmail(String email) {
        for (User student : students) {
            if (student.getEmail().equalsIgnoreCase(email)) {
                return student;
            }
        }
        return null;
    }

    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Admin a : admins)
                writer.println("ADMIN|" + a.getName() + "|" + a.getEmail() + "|" + a.getPassword());
            for (User u : coordinators)
                writer.println("COORD|" + u.getName() + "|" + u.getEmail() + "|" + u.getPassword());
            for (User u : students)
                writer.println("STUDENT|" + u.getName() + "|" + u.getEmail() + "|" + u.getPassword());
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 4)
                    continue;
                String role = parts[0];
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];

                switch (role) {
                    case "ADMIN":
                        admins.add(new Admin(name, email, password));
                        break;
                    case "COORD":
                        coordinators.add(new User(name, email, password));
                        break;
                    case "STUDENT":
                        students.add(new User(name, email, password));
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
}
