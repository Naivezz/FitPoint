package com.naivez.fithub.repository;

import com.naivez.fithub.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUserIdOrderByEndDateDesc(Long userId);

    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.active = true AND m.endDate >= :today ORDER BY m.endDate DESC")
    List<Membership> findActiveByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.endDate >= :today ORDER BY m.endDate DESC")
    List<Membership> findValidByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);
}
