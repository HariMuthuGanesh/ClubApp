package com.clubapp.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NewsResponse {
    private Long id;
    private String title;
    private String content;
    private Long clubId;
    private String clubName;
    private String clubDept;
    private LocalDateTime postedAt;
}
