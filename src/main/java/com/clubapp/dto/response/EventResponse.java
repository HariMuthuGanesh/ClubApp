package com.clubapp.dto.response;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private String venue;
    private String date;
    private String time;
    private String posterImage;
    private boolean membersOnly;
    private Long clubId;
    private String clubName;
    private int attendeeCount;
    private Integer maxAttendees;
    private boolean isRegistered;
    private boolean isFull;
    private String winners;
}
