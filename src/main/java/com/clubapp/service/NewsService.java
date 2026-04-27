package com.clubapp.service;

import com.clubapp.dto.response.NewsResponse;
import com.clubapp.entity.Club;
import com.clubapp.entity.News;
import com.clubapp.entity.Role;
import com.clubapp.entity.User;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public NewsResponse postNews(Long clubId, String title, String content, User currentUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        
        if (currentUser.getRole() == Role.COORDINATOR && 
            (club.getCoordinator() == null || !club.getCoordinator().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("You can only post news for your own club.");
        }

        News news = News.builder()
                .title(title)
                .content(content)
                .club(club)
                .build();
        
        return mapToResponse(newsRepository.save(news));
    }

    public List<NewsResponse> getNewsForUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return newsRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return newsRepository.findNewsForUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NewsResponse> getNewsByClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found."));
        return newsRepository.findByClub(club).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteNews(Long id, User currentUser) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found."));
        
        if (currentUser.getRole() == Role.COORDINATOR && 
            (news.getClub().getCoordinator() == null || !news.getClub().getCoordinator().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Access denied.");
        }
        
        newsRepository.delete(news);
    }

    private NewsResponse mapToResponse(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .clubId(news.getClub().getId())
                .clubName(news.getClub().getName())
                .clubDept(news.getClub().getDepartment())
                .postedAt(news.getPostedAt())
                .build();
    }
}
