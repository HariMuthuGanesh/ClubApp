package com.clubapp.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Event name is required") private String name;
    private String description;
    private String venue;
    private String date;
    private String time;
    private boolean membersOnly;
    private Integer maxAttendees;
}
