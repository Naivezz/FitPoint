package com.naivez.fithub.service;

import com.naivez.fithub.dto.NotificationDTO;
import com.naivez.fithub.entity.Notification;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.NotificationMapper;
import com.naivez.fithub.repository.NotificationRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public List<NotificationDTO> getUserNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByRecipientId(user.getId());

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findUnreadByRecipientId(user.getId());

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(String userEmail, Long notificationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findUnreadByRecipientId(user.getId());

        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void createNotification(User recipient, String message) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotificationByUserId(Long recipientId, String message) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        createNotification(recipient, message);
    }
}