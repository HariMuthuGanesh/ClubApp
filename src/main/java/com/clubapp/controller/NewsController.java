package com.clubapp.controller;

import com.clubapp.dto.response.NewsResponse;
import com.clubapp.entity.User;
import com.clubapp.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<NewsResponse> postNews(@RequestBody Map<String, Object> req,
                                                 @AuthenticationPrincipal User currentUser) {
        Long clubId = Long.valueOf(req.get("clubId").toString());
        String title = req.get("title").toString();
        String content = req.get("content").toString();
        return ResponseEntity.ok(newsService.postNews(clubId, title, content, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<NewsResponse>> getMyNewsFeed(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(newsService.getNewsForUser(currentUser));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<NewsResponse>> getNewsByClub(@PathVariable Long clubId) {
        return ResponseEntity.ok(newsService.getNewsByClub(clubId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id,
                                           @AuthenticationPrincipal User currentUser) {
        newsService.deleteNews(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
