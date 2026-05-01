package com.clubapp.repository;
import com.clubapp.entity.Event;
import com.clubapp.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByClub(Club club);
    List<Event> findByAttendeesContaining(com.clubapp.entity.User user);
}
