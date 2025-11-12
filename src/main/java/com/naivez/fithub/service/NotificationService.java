package com.naivez.fithub.service;

import com.naivez.fithub.dto.NotificationDTO;
import com.naivez.fithub.entity.Notification;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.NotificationMapper;
import com.naivez.fithub.repository.NotificationRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.debug("Marking notification as read - user: {}, notificationId: {}", userEmail, notificationId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            log.warn("Attempt to mark foreign notification as read - user: {}, notificationId: {}",
                    userEmail, notificationId);
            throw new RuntimeException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        log.debug("Notification marked as read - user: {}, notificationId: {}", userEmail, notificationId);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        log.debug("Marking all notifications as read for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findUnreadByRecipientId(user.getId());

        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.debug("All notifications marked as read for user: {}", userEmail);
    }

    @Transactional
    public void createNotification(User recipient, String message) {
        log.debug("Creating notification for user: {}", recipient.getEmail());

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