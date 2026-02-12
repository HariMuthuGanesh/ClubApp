package Clubs;

public class CseAsso extends Club {
    public CseAsso() {
        super("CSE Association", "Computer Science and Engineering Association");
    }

    @Override
    public void viewDetails() {
        System.out.println(getName() + " Details:");
        System.out.println("Name: " + getName());
        System.out.println("Description: " + getDescription());
    }
}
