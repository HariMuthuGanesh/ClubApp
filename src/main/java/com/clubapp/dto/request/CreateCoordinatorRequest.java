package com.clubapp.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCoordinatorRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank private String password;
    private String department;
}
