package com.naivez.fithub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "training_classes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    private int capacity;

    @OneToMany(mappedBy = "trainingClass", cascade = CascadeType.ALL)
    private Set<Reservation> reservations = new HashSet<>();

    @Column(name = "average_rating")
    private Double averageRating;
}