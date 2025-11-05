package com.naivez.fithub.repository;

import com.naivez.fithub.entity.PersonalTrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PersonalTrainingSessionRepository extends JpaRepository<PersonalTrainingSession, Long> {

    @Query("SELECT pts FROM PersonalTrainingSession pts WHERE pts.trainer.id = :trainerId ORDER BY pts.startTime DESC")
    List<PersonalTrainingSession> findByTrainerId(@Param("trainerId") Long trainerId);

    @Query("SELECT pts FROM PersonalTrainingSession pts WHERE pts.trainer.id = :trainerId AND pts.startTime BETWEEN :start AND :end ORDER BY pts.startTime ASC")
    List<PersonalTrainingSession> findByTrainerIdAndDateRange(@Param("trainerId") Long trainerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT pts FROM PersonalTrainingSession pts WHERE pts.trainer.id = :trainerId AND pts.client.id = :clientId ORDER BY pts.startTime DESC")
    List<PersonalTrainingSession> findByTrainerIdAndClientId(@Param("trainerId") Long trainerId, @Param("clientId") Long clientId);
}