package com.clubapp.repository;

import com.clubapp.entity.Club;
import com.clubapp.entity.News;
import com.clubapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    List<News> findByClub(Club club);

    @Query("SELECT n FROM News n WHERE n.club.department = 'ALL' OR :user MEMBER OF n.club.members OR n.club.coordinator = :user ORDER BY n.postedAt DESC")
    List<News> findNewsForUser(@Param("user") User user);
    
    void deleteByClub(Club club);
}
