package clubs;

import manage.User;
import java.util.ArrayList;
import java.util.List;

public class Club {
    private String clubName;
    private String description;
    private User coordinator;
    private List<Event> events = new ArrayList<>();
    private List<User> members = new ArrayList<>();

    public Club(String clubName, String description, User coordinator) {
        this.clubName = clubName;
        this.description = description;
        this.coordinator = coordinator;
    }

    public String getClubName() {
        return clubName;
    }

    public String getDescription() {
        return description;
    }

    public User getCoordinator() {
        return coordinator;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<User> getMembers() {
        return members;
    }

    public void viewDetails() {
        System.out.println("Club Name: " + clubName);
        System.out.println("Description: " + description);
        System.out.println("Coordinator: " + coordinator.getName() + " (" + coordinator.getEmail() + ")");
        System.out.println("Total Events: " + events.size());
    }

    public void addEvent(Event event) {
        events.add(event);
        System.out.println("Event added successfully.");
    }

    public void listEvents() {
        if (events.isEmpty()) {
            System.out.println("No events scheduled.");
        } else {
            for (int i = 0; i < events.size(); i++) {
                System.out.println((i + 1) + ". " + events.get(i));
            }
        }
    }

    public void removeEvent(int index) {
        if (index >= 0 && index < events.size()) {
            events.remove(index);
            System.out.println("Event removed successfully.");
        } else {
            System.out.println("Invalid event index.");
        }
    }

    public void updateEvent(int index, Event newEvent) {
        if (index >= 0 && index < events.size()) {
            events.set(index, newEvent);
            System.out.println("Event updated successfully.");
        } else {
            System.out.println("Invalid event index.");
        }
    }

    public void addMember(User student) {
        members.add(student);
    }

    public boolean isMember(User student) {
        for (User member : members) {
            if (member.getEmail().equalsIgnoreCase(student.getEmail())) {
                return true;
            }
        }
        return false;
    }

    public boolean registerStudentForEvent(User student, int eventIndex) {
        if (eventIndex >= 0 && eventIndex < events.size()) {
            Event event = events.get(eventIndex);
            if (event.isRegistered(student)) {
                System.out.println("Already registered for this event.");
                return false;
            }
            event.addAttendee(student);
            return true;
        }
        System.out.println("Invalid event index.");
        return false;
    }
}
