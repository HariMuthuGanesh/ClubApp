package com.clubapp.dto.request;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String department;
    private String year;
    private String bio;
}
