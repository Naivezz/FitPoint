package com.naivez.fithub.repository;

import com.naivez.fithub.entity.Reservation;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByReservationDateDesc(Long userId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.trainingClass tc WHERE r.user.id = :userId ORDER BY tc.startTime DESC")
    List<Reservation> findByUserWithTrainingClass(@Param("userId") Long userId);

    Optional<Reservation> findByUserAndTrainingClass(User user, TrainingClass trainingClass);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.trainingClass.id = :classId AND r.status = 'CONFIRMED'")
    long countConfirmedReservationsByClassId(@Param("classId") Long classId);

    boolean existsByUserAndTrainingClassAndStatus(User user, TrainingClass trainingClass, String status);
}
