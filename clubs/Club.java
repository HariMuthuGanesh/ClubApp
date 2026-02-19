
public class Club {
    private String clubName;
    private String description;
    private User coordinator;
    private Event[] events = new Event[100];
    private int eventCount = 0;

    public Club(String clubName, String description, User coordinator) {
        this.clubName = clubName;
        this.description = description;
        this.coordinator = coordinator;
    }

    public String getClubName() {
        return clubName;
    }

    public User getCoordinator() {
        return coordinator;
    }

    public void viewDetails() {
        System.out.println("Club Name: " + clubName);
        System.out.println("Description: " + description);
        System.out.println("Coordinator: " + coordinator.getName() + " (" + coordinator.getEmail() + ")");
        System.out.println("Total Events: " + eventCount);
    }

    public void addEvent(Event event) {
        if (eventCount < 100) {
            events[eventCount++] = event;
            System.out.println("Event added successfully.");
        } else {
            System.out.println("Event limit reached.");
        }
    }

    public void listEvents() {
        if (eventCount == 0) {
            System.out.println("No events scheduled.");
        } else {
            for (int i = 0; i < eventCount; i++) {
                System.out.println((i + 1) + ". " + events[i]);
            }
        }
    }

    public void removeEvent(int index) {
        if (index >= 0 && index < eventCount) {
            for (int i = index; i < eventCount - 1; i++) {
                events[i] = events[i + 1];
            }
            events[--eventCount] = null;
            System.out.println("Event removed successfully.");
        } else {
            System.out.println("Invalid event index.");
        }
    }

    public void updateEvent(int index, Event newEvent) {
        if (index >= 0 && index < eventCount) {
            events[index] = newEvent;
            System.out.println("Event updated successfully.");
        } else {
            System.out.println("Invalid event index.");
        }
    }
}
