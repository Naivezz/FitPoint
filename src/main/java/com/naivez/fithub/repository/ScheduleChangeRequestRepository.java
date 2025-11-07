package com.naivez.fithub.repository;

import com.naivez.fithub.entity.ScheduleChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleChangeRequestRepository extends JpaRepository<ScheduleChangeRequest, Long> {

    @Query("SELECT scr FROM ScheduleChangeRequest scr WHERE scr.trainer.id = :trainerId ORDER BY scr.createdAt DESC")
    List<ScheduleChangeRequest> findByTrainerId(@Param("trainerId") Long trainerId);

    @Query("SELECT scr FROM ScheduleChangeRequest scr WHERE scr.trainer.id = :trainerId AND scr.status = :status ORDER BY scr.createdAt DESC")
    List<ScheduleChangeRequest> findByTrainerIdAndStatus(@Param("trainerId") Long trainerId, @Param("status") String status);

    @Query("SELECT scr FROM ScheduleChangeRequest scr WHERE scr.status = :status ORDER BY scr.createdAt DESC")
    List<ScheduleChangeRequest> findByStatus(@Param("status") String status);
}