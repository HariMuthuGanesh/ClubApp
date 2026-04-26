package com.clubapp.dto.response;
import com.clubapp.entity.Role;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String department;
    private String year;
    private String bio;
}
