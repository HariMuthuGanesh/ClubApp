package Clubs;

import java.util.Scanner;

public abstract class Club {
    private String name;
    private String description;
    private Event[] events = new Event[100];
    private int eventCount = 0;

    public Club(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract void viewDetails();

    public void addEvent(Scanner sc) {
        System.out.print("Enter event name: ");
        String eventName = sc.nextLine();
        System.out.print("Enter event description: ");
        String desc = sc.nextLine();
        System.out.print("Enter event venue: ");
        String venue = sc.nextLine();
        System.out.print("Enter event date (dd/mm/yyyy): ");
        String date = sc.nextLine();

        if (eventCount < events.length) {
            events[eventCount++] = new Event(eventName, desc, venue, date);
            System.out.println("Event '" + eventName + "' added successfully to " + name + "!");
        } else {
            System.out.println("Event list is full for " + name + ".");
        }
    }

    public void listEvents() {
        System.out.println("Listing all events for " + name + ":");
        if (eventCount == 0) {
            System.out.println("No events scheduled yet.");
        } else {
            for (int i = 0; i < eventCount; i++) {
                System.out.println((i + 1) + ". " + events[i]);
            }
        }
    }

    // public void removeEvent(String eventName) {
    // System.out.println("Removing event: " + eventName + " from " + name + "
    // (Feature in progress)");
    // }

    // public void updateEvent(String oldEvent, String newEvent) {
    // System.out
    // .println("Updating event: " + oldEvent + " to " + newEvent + " in " + name +
    // " (Feature in progress)");
    // }

    // public void viewMembers() {
    // System.out.println("Viewing all members of " + name + " (Feature in
    // progress)");
    // }
}
