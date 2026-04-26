package com.clubapp.dto.response;
import com.clubapp.entity.JoinRequestStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JoinRequestResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userDepartment;
    private String userYear;
    private Long clubId;
    private String clubName;
    private JoinRequestStatus status;
    private String message;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
}
