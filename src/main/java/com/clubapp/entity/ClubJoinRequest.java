package com.clubapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "club_join_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClubJoinRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private LocalDateTime requestedAt;

    @Column
    private LocalDateTime respondedAt;

    @PrePersist
    public void prePersist() { this.requestedAt = LocalDateTime.now(); }
}
