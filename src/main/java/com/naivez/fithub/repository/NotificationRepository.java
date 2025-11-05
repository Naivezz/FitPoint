package com.naivez.fithub.repository;

import com.naivez.fithub.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.sentAt DESC")
    List<Notification> findByRecipientId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.read = false ORDER BY n.sentAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("userId") Long userId);
}