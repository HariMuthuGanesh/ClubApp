
public class ClubManager {
    private Club[] clubs = new Club[50];
    private int clubCount = 0;

    public void addClub(Club club) {
        if (clubCount < 50) {
            clubs[clubCount++] = club;
        }
    }

    public void listAllClubs() {
        if (clubCount == 0) {
            System.out.println("No clubs registered.");
        } else {
            for (int i = 0; i < clubCount; i++) {
                System.out.println((i + 1) + ". " + clubs[i].getClubName());
            }
        }
    }

    public Club findClubByName(String name) {
        for (int i = 0; i < clubCount; i++) {
            if (clubs[i].getClubName().equalsIgnoreCase(name)) {
                return clubs[i];
            }
        }
        return null;
    }

    public Club getClubByCoordinator(User user) {
        for (int i = 0; i < clubCount; i++) {
            if (clubs[i].getCoordinator() == user) {
                return clubs[i];
            }
        }
        return null;
    }

    public Club getClubByIndex(int index) {
        if (index >= 0 && index < clubCount) {
            return clubs[index];
        }
        return null;
    }

    public int getClubCount() {
        return clubCount;
    }
}
