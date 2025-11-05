package com.naivez.fithub.repository;

import com.naivez.fithub.entity.TrainingClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainingClassRepository extends JpaRepository<TrainingClass, Long> {

    @Query("SELECT tc FROM TrainingClass tc WHERE tc.startTime > :now ORDER BY tc.startTime ASC")
    List<TrainingClass> findUpcomingClasses(@Param("now") LocalDateTime now);

    @Query("SELECT tc FROM TrainingClass tc WHERE tc.startTime BETWEEN :start AND :end ORDER BY tc.startTime ASC")
    List<TrainingClass> findClassesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT tc FROM TrainingClass tc WHERE tc.trainer.id = :trainerId AND tc.startTime BETWEEN :start AND :end ORDER BY tc.startTime ASC")
    List<TrainingClass> findByTrainerIdAndDateRange(@Param("trainerId") Long trainerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
