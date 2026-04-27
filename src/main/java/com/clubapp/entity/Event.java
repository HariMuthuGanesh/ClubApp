package com.clubapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String venue;

    @Column
    private String date;

    @Column
    private String time;

    @Column
    private String posterImage;

    @Column(nullable = false)
    private boolean membersOnly;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(columnDefinition = "TEXT")
    private String winners;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnore
    private Club club;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> attendees = new HashSet<>();
}
