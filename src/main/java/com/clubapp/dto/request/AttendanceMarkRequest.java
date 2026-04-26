package com.clubapp.dto.request;

import com.clubapp.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceMarkRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Status is required (PRESENT or ABSENT)")
    private AttendanceStatus status;
}
