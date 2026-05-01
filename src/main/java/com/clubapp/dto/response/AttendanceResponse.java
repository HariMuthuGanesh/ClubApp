package com.clubapp.dto.response;

import com.clubapp.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String email;
    private Long eventId;
    private String eventName;
    private AttendanceStatus status;
    private String utterance;
}
