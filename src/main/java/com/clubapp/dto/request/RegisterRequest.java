package com.clubapp.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required") private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") private String password;
    private String department;
    private String year;
}
