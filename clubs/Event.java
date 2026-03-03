package clubs;

import manage.User;

public class Event {
    private String name;
    private String description;
    private String venue;
    private String date;
    private java.util.List<User> attendees = new java.util.ArrayList<>();

    public Event(String name, String description, String venue, String date) {
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVenue() {
        return venue;
    }

    public String getDate() {
        return date;
    }

    public java.util.List<User> getAttendees() {
        return attendees;
    }

    public void addAttendee(User student) {
        if (!isRegistered(student)) {
            attendees.add(student);
        }
    }

    public boolean isRegistered(User student) {
        for (User u : attendees) {
            if (u.getEmail().equalsIgnoreCase(student.getEmail())) {
                return true;
            }
        }
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Event: " + name + " | Description: " + description + " | Venue: " + venue + " | Date: " + date
                + " | Registered: " + attendees.size();
    }
}
