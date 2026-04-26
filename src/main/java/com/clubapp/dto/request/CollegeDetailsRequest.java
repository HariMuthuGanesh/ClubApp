package com.clubapp.dto.request;
import lombok.Data;

@Data
public class CollegeDetailsRequest {
    private String collegeName;
    private String location;
    private String established;
    private String tneaCode;
    private String principalName;
    private String website;
    private String vision;
    private String mission;
    private String about;
}
