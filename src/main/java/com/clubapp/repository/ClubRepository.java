package com.clubapp.repository;
import com.clubapp.entity.Club;
import com.clubapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ClubRepository extends JpaRepository<Club, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Club> findByCoordinator(User coordinator);
}
