package com.clubapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollegeDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String collegeName;

    @Column
    private String location;

    @Column
    private String established;

    @Column
    private String tneaCode;

    @Column
    private String principalName;

    @Column
    private String website;

    @Column(columnDefinition = "TEXT")
    private String vision;

    @Column(columnDefinition = "TEXT")
    private String mission;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column
    private String logoImage;

    @Column
    private String bannerImage;
}
