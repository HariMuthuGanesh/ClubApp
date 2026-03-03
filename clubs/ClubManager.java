package clubs;

import manage.User;
import manage.UserManager;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class ClubManager {
    private List<Club> clubs = new ArrayList<>();
    private static final String CLUBS_FILE = "file/clubs.txt";
    private static final String EVENTS_FILE = "file/events.txt";
    private static final String MEMBERS_FILE = "file/members.txt";
    private static final String REGISTRATIONS_FILE = "file/event_registrations.txt";

    public void addClub(Club club) {
        clubs.add(club);
        saveAll();
    }

    public void listAllClubs() {
        if (clubs.isEmpty()) {
            System.out.println("No clubs registered.");
        } else {
            for (int i = 0; i < clubs.size(); i++) {
                System.out.println((i + 1) + ". " + clubs.get(i).getClubName());
            }
        }
    }

    public Club findClubByName(String name) {
        for (Club club : clubs) {
            if (club.getClubName().equalsIgnoreCase(name)) {
                return club;
            }
        }
        return null;
    }

    public Club getClubByCoordinator(User user) {
        for (Club club : clubs) {
            if (club.getCoordinator().getEmail().equalsIgnoreCase(user.getEmail())) {
                return club;
            }
        }
        return null;
    }

    public Club getClubByIndex(int index) {
        if (index >= 0 && index < clubs.size()) {
            return clubs.get(index);
        }
        return null;
    }

    public int getClubCount() {
        return clubs.size();
    }

    public void listJoinedClubs(User student) {
        boolean found = false;
        for (Club club : clubs) {
            if (club.isMember(student)) {
                System.out.println("- " + club.getClubName());
                found = true;
            }
        }
        if (!found) {
            System.out.println("You haven't joined any clubs yet.");
        }
    }

    public void joinClub(User student, int clubIdx) {
        Club club = getClubByIndex(clubIdx);
        if (club != null) {
            if (club.isMember(student)) {
                System.out.println("You are already a member of this club!");
            } else {
                club.addMember(student);
                saveAll();
                System.out.println("Successfully joined " + club.getClubName() + "!");
            }
        } else {
            System.out.println("Invalid club selection.");
        }
    }

    public void saveAll() {
        try (PrintWriter clubWriter = new PrintWriter(new FileWriter(CLUBS_FILE));
                PrintWriter eventWriter = new PrintWriter(new FileWriter(EVENTS_FILE));
                PrintWriter memberWriter = new PrintWriter(new FileWriter(MEMBERS_FILE))) {

            for (Club club : clubs) {
                clubWriter.println(
                        club.getClubName() + "|" + club.getDescription() + "|" + club.getCoordinator().getEmail());

                for (Event e : club.getEvents()) {
                    eventWriter.println(club.getClubName() + "|" + e.getName() + "|" + e.getDescription() + "|"
                            + e.getVenue() + "|" + e.getDate());
                }

                for (User m : club.getMembers()) {
                    memberWriter.println(club.getClubName() + "|" + m.getEmail());
                }
            }

            try (PrintWriter regWriter = new PrintWriter(new FileWriter(REGISTRATIONS_FILE))) {
                for (Club club : clubs) {
                    for (Event e : club.getEvents()) {
                        for (User attendee : e.getAttendees()) {
                            regWriter.println(club.getClubName() + "|" + e.getName() + "|" + attendee.getEmail());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving club data: " + e.getMessage());
        }
    }

    public void loadAll(UserManager userManager) {
        clubs.clear();
        File clubFile = new File(CLUBS_FILE);
        if (!clubFile.exists())
            return;

        // Load Clubs
        try (BufferedReader reader = new BufferedReader(new FileReader(clubFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 3)
                    continue;
                String name = parts[0];
                String desc = parts[1];
                String coordEmail = parts[2];
                User coord = userManager.findCoordinatorByEmail(coordEmail);
                if (coord != null) {
                    clubs.add(new Club(name, desc, coord));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading clubs: " + e.getMessage());
        }

        // Load Events
        File eventFile = new File(EVENTS_FILE);
        if (eventFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(eventFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length < 5)
                        continue;
                    Club club = findClubByName(parts[0]);
                    if (club != null) {
                        club.getEvents().add(new Event(parts[1], parts[2], parts[3], parts[4]));
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading events: " + e.getMessage());
            }
        }

        // Load Memberships
        File memberFile = new File(MEMBERS_FILE);
        if (memberFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(memberFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length < 2)
                        continue;
                    Club club = findClubByName(parts[0]);
                    User member = userManager.findStudentByEmail(parts[1]);
                    if (club != null && member != null) {
                        club.addMember(member);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading members: " + e.getMessage());
            }
        }

        // Load Event Registrations
        File regFile = new File(REGISTRATIONS_FILE);
        if (regFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(regFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length < 3)
                        continue;
                    Club club = findClubByName(parts[0]);
                    if (club != null) {
                        for (Event e : club.getEvents()) {
                            if (e.getName().equalsIgnoreCase(parts[1])) {
                                User student = userManager.findStudentByEmail(parts[2]);
                                if (student != null) {
                                    e.addAttendee(student);
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading registrations: " + e.getMessage());
            }
        }
    }
}
