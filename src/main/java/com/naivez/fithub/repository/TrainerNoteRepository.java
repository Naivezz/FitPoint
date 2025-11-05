package com.naivez.fithub.repository;

import com.naivez.fithub.entity.TrainerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainerNoteRepository extends JpaRepository<TrainerNote, Long> {

    @Query("SELECT tn FROM TrainerNote tn WHERE tn.trainer.id = :trainerId ORDER BY tn.createdAt DESC")
    List<TrainerNote> findByTrainerId(@Param("trainerId") Long trainerId);

    @Query("SELECT tn FROM TrainerNote tn WHERE tn.trainer.id = :trainerId AND tn.client.id = :clientId ORDER BY tn.createdAt DESC")
    List<TrainerNote> findByTrainerIdAndClientId(@Param("trainerId") Long trainerId, @Param("clientId") Long clientId);
}