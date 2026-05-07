package com.clubapp.repository;
import com.clubapp.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ClubJoinRequestRepository extends JpaRepository<ClubJoinRequest, Long> {
    List<ClubJoinRequest> findByClubAndStatus(Club club, JoinRequestStatus status);
    List<ClubJoinRequest> findByUser(User user);
    List<ClubJoinRequest> findByClub(Club club);
    Optional<ClubJoinRequest> findByUserAndClub(User user, Club club);
    boolean existsByUserAndClubAndStatus(User user, Club club, JoinRequestStatus status);
    void deleteByUser(User user);
}
