package com.clubapp.repository;

import com.clubapp.entity.Attendance;
import com.clubapp.entity.Event;
import com.clubapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Replaces: AttendanceManager.java file I/O methods */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEvent(Event event);
    List<Attendance> findByEventId(Long eventId);
    Optional<Attendance> findByEventAndUser(Event event, User user);
    void deleteByEvent(Event event);
    void deleteByUser(User user);
}
