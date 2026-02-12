package Clubs;

public class Ieee extends Club {
    public Ieee() {
        super("IEEE",
                "Institute of Electrical and Electronics Engineers - A club for electrical and electronics engineering enthusiasts.");
    }

    @Override
    public void viewDetails() {
        System.out.println(getName() + " Details:");
        System.out.println("Name: " + getName());
        System.out.println("Description: " + getDescription());
    }
}
