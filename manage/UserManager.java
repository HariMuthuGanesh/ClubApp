
public class UserManager {
    private Admin[] admins = new Admin[100];
    private User[] coordinators = new User[100];
    private User[] students = new User[100];

    private int adminCount = 0;
    private int coordinatorCount = 0;
    private int studentCount = 0;

    public void addAdmin(Admin admin) {
        if (adminCount < 100) {
            admins[adminCount++] = admin;
        }
    }

    public void addCoordinator(User coordinator) {
        if (coordinatorCount < 100) {
            coordinators[coordinatorCount++] = coordinator;
        }
    }

    public void addStudent(User student) {
        if (studentCount < 100) {
            students[studentCount++] = student;
        }
    }

    public Admin validateAdmin(String email, String password) {
        for (int i = 0; i < adminCount; i++) {
            if (admins[i].getEmail().equalsIgnoreCase(email) && admins[i].getPassword().equals(password)) {
                return admins[i];
            }
        }
        return null;
    }

    public User validateCoordinator(String email, String password) {
        for (int i = 0; i < coordinatorCount; i++) {
            if (coordinators[i].getEmail().equalsIgnoreCase(email) && coordinators[i].getPassword().equals(password)) {
                return coordinators[i];
            }
        }
        return null;
    }

    public User validateStudent(String email, String password) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getEmail().equalsIgnoreCase(email) && students[i].getPassword().equals(password)) {
                return students[i];
            }
        }
        return null;
    }

    public User findCoordinatorByEmail(String email) {
        for (int i = 0; i < coordinatorCount; i++) {
            if (coordinators[i].getEmail().equalsIgnoreCase(email)) {
                return coordinators[i];
            }
        }
        return null;
    }

    public User findStudentByEmail(String email) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getEmail().equalsIgnoreCase(email)) {
                return students[i];
            }
        }
        return null;
    }
}
