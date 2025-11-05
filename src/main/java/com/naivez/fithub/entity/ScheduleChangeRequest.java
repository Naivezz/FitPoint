package com.naivez.fithub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_change_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_class_id")
    private TrainingClass trainingClass;

    @Column(nullable = false)
    private String requestType;

    @Column(length = 1000)
    private String reason;

    private String className;

    private String classDescription;

    @Column(name = "requested_start_time")
    private LocalDateTime requestedStartTime;

    @Column(name = "requested_end_time")
    private LocalDateTime requestedEndTime;

    private Integer requestedCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_room_id")
    private Room requestedRoom;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(length = 1000)
    private String reviewNote;
}
