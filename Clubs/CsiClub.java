package Clubs;

public class CsiClub extends Club {
    public CsiClub() {
        super("CSI Club", "Computer Society of India - A club for computer science enthusiasts.");
    }

    @Override
    public void viewDetails() {
        System.out.println(getName() + " Details:");
        System.out.println("Name: " + getName());
        System.out.println("Description: " + getDescription());
        System.out.println("Events: Coding competitions, workshops, guest lectures.");
    }
}
