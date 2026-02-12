package Clubs;

public class Event {
    private String name;
    private String description;
    private String venue;
    private String date;

    public Event(String name, String description, String venue, String date) {
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return name + " - " + description + " at " + venue + " on " + date;
    }
}
