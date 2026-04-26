package com.clubapp.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClubRequest {
    @NotBlank(message = "Club name is required") private String name;
    private String description;
    private String vision;
    private String mission;
    private String department;
    private Integer foundedYear;
    private String coordinatorEmail;
}
