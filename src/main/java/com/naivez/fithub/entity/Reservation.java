package com.naivez.fithub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_class_id", nullable = false)
    private TrainingClass trainingClass;

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(nullable = false)
    private String status;

    private Integer rating;

    @Column(length = 1000)
    private String comment;
}