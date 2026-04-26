package com.clubapp.dto.response;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClubResponse {
    private Long id;
    private String name;
    private String description;
    private String vision;
    private String mission;
    private String department;
    private Integer foundedYear;
    private String logoImage;
    private String coordinatorName;
    private String coordinatorEmail;
    private Long coordinatorId;
    private int memberCount;
    private int eventCount;
    private boolean isMember;
    private String joinRequestStatus;
}
