package com.clubapp.dto.response;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CollegeDetailsResponse {
    private Long id;
    private String collegeName;
    private String location;
    private String established;
    private String tneaCode;
    private String principalName;
    private String website;
    private String vision;
    private String mission;
    private String about;
    private String logoImage;
    private String bannerImage;
}
