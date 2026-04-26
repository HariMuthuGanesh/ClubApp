package com.clubapp.service;

import com.clubapp.dto.request.CollegeDetailsRequest;
import com.clubapp.dto.response.CollegeDetailsResponse;
import com.clubapp.entity.CollegeDetails;
import com.clubapp.repository.CollegeDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollegeDetailsService {

    private final CollegeDetailsRepository repo;

    public CollegeDetailsResponse getDetails() {
        return repo.findAll().stream().findFirst()
                .map(this::mapToResponse)
                .orElse(CollegeDetailsResponse.builder()
                        .collegeName("Your College Name")
                        .location("Location")
                        .established("—")
                        .tneaCode("—")
                        .build());
    }

    @Transactional
    public CollegeDetailsResponse saveDetails(CollegeDetailsRequest req) {
        CollegeDetails details = repo.findAll().stream().findFirst()
                .orElse(new CollegeDetails());
        if (req.getCollegeName()   != null) details.setCollegeName(req.getCollegeName());
        if (req.getLocation()      != null) details.setLocation(req.getLocation());
        if (req.getEstablished()   != null) details.setEstablished(req.getEstablished());
        if (req.getTneaCode()      != null) details.setTneaCode(req.getTneaCode());
        if (req.getPrincipalName() != null) details.setPrincipalName(req.getPrincipalName());
        if (req.getWebsite()       != null) details.setWebsite(req.getWebsite());
        if (req.getVision()        != null) details.setVision(req.getVision());
        if (req.getMission()       != null) details.setMission(req.getMission());
        if (req.getAbout()         != null) details.setAbout(req.getAbout());
        return mapToResponse(repo.save(details));
    }

    @Transactional
    public CollegeDetailsResponse updateLogo(String filename) {
        CollegeDetails details = repo.findAll().stream().findFirst()
                .orElse(new CollegeDetails());
        details.setLogoImage(filename);
        return mapToResponse(repo.save(details));
    }

    public CollegeDetailsResponse mapToResponse(CollegeDetails d) {
        return CollegeDetailsResponse.builder()
                .id(d.getId())
                .collegeName(d.getCollegeName())
                .location(d.getLocation())
                .established(d.getEstablished())
                .tneaCode(d.getTneaCode())
                .principalName(d.getPrincipalName())
                .website(d.getWebsite())
                .vision(d.getVision())
                .mission(d.getMission())
                .about(d.getAbout())
                .logoImage(d.getLogoImage())
                .bannerImage(d.getBannerImage())
                .build();
    }
}
