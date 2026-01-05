package com.naivez.fithub.controller;

import com.naivez.fithub.dto.CreateNotificationRequest;
import com.naivez.fithub.dto.NotificationDTO;
import com.naivez.fithub.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDTO> notifications =
                notificationService.getUserNotifications(userDetails.getUsername());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDTO> notifications =
                notificationService.getUnreadNotifications(userDetails.getUsername());
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(userDetails.getUsername(), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/unread")
    public ResponseEntity<Integer> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDTO> notifications =
                notificationService.getUnreadNotifications(userDetails.getUsername());
        return ResponseEntity.ok(notifications.size());
    }

    @PostMapping
    public ResponseEntity<Void> createNotification(@RequestBody @Valid CreateNotificationRequest request) {
        notificationService.createNotification(
                request.getRecipientEmail(),
                request.getMessage()
        );
        return ResponseEntity.ok().build();
    }
}