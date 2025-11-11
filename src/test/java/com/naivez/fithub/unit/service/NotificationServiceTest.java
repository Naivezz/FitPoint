package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.NotificationDTO;
import com.naivez.fithub.entity.Notification;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.NotificationMapper;
import com.naivez.fithub.repository.NotificationRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;
    private NotificationDTO testNotificationDTO;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30);

        testUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .build();

        testNotification = Notification.builder()
                .id(1L)
                .recipient(testUser)
                .message("Welcome")
                .sentAt(testDateTime)
                .read(false)
                .build();

        testNotificationDTO = NotificationDTO.builder()
                .id(1L)
                .message("Welcome")
                .sentAt(testDateTime)
                .read(false)
                .build();
    }

    @Test
    void getUserNotifications_whenUserExists_shouldReturnNotificationList() {
        Notification notification2 = Notification.builder()
                .id(2L)
                .recipient(testUser)
                .message("message")
                .sentAt(testDateTime.plusDays(1))
                .read(true)
                .build();

        NotificationDTO notificationDTO2 = NotificationDTO.builder()
                .id(2L)
                .message("message")
                .sentAt(testDateTime.plusDays(1))
                .read(true)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByRecipientId(1L)).thenReturn(List.of(testNotification, notification2));
        when(notificationMapper.toDto(testNotification)).thenReturn(testNotificationDTO);
        when(notificationMapper.toDto(notification2)).thenReturn(notificationDTO2);

        List<NotificationDTO> result = notificationService.getUserNotifications("user1@gmail.com");

        assertThat(result).hasSize(2);
        assertThat(result).contains(testNotificationDTO, notificationDTO2);
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findByRecipientId(1L);
        verify(notificationMapper, times(2)).toDto(any(Notification.class));
    }

    @Test
    void getUserNotifications_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getUserNotifications("none@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(notificationRepository, never()).findByRecipientId(anyLong());
    }

    @Test
    void getUserNotifications_whenUserHasNoNotifications_shouldReturnEmptyList() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByRecipientId(1L)).thenReturn(List.of());

        List<NotificationDTO> result = notificationService.getUserNotifications("user1@gmail.com");

        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findByRecipientId(1L);
        verify(notificationMapper, never()).toDto(any(Notification.class));
    }

    @Test
    void getUnreadNotifications_whenUserHasUnreadNotifications_shouldReturnOnlyUnread() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findUnreadByRecipientId(1L)).thenReturn(List.of(testNotification));
        when(notificationMapper.toDto(testNotification)).thenReturn(testNotificationDTO);

        List<NotificationDTO> result = notificationService.getUnreadNotifications("user1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testNotificationDTO);
        assertThat(result.get(0).isRead()).isFalse();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findUnreadByRecipientId(1L);
        verify(notificationMapper).toDto(testNotification);
    }

    @Test
    void getUnreadNotifications_whenUserHasNoUnreadNotifications_shouldReturnEmptyList() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findUnreadByRecipientId(1L)).thenReturn(List.of());

        List<NotificationDTO> result = notificationService.getUnreadNotifications("user1@gmail.com");

        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findUnreadByRecipientId(1L);
        verify(notificationMapper, never()).toDto(any(Notification.class));
    }

    @Test
    void getUnreadNotifications_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getUnreadNotifications("none@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(notificationRepository, never()).findUnreadByRecipientId(anyLong());
    }

    @Test
    void markAsRead_whenValidNotification_shouldMarkAsRead() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsRead("user1@gmail.com", 1L);

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAsRead_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("none@gmail.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(notificationRepository, never()).findById(anyLong());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_whenNotificationNotFound_shouldThrowException() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("user1@gmail.com", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_whenNotificationBelongsToOtherUser_shouldThrowException() {
        User otherUser = User.builder()
                .id(2L)
                .email("user1@gmail.com")
                .build();

        Notification otherNotification = Notification.builder()
                .id(2L)
                .recipient(otherUser)
                .message("Other user notification")
                .sentAt(testDateTime)
                .read(false)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(otherNotification));

        assertThatThrownBy(() -> notificationService.markAsRead("user1@gmail.com", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only mark your own notifications as read");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findById(2L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_whenUserHasUnreadNotifications_shouldMarkAllAsRead() {
        Notification unreadNotification2 = Notification.builder()
                .id(2L)
                .recipient(testUser)
                .message("Second unread notification")
                .sentAt(testDateTime.plusHours(1))
                .read(false)
                .build();

        List<Notification> unreadNotifications = List.of(testNotification, unreadNotification2);

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findUnreadByRecipientId(1L)).thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(unreadNotifications)).thenReturn(unreadNotifications);

        notificationService.markAllAsRead("user1@gmail.com");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findUnreadByRecipientId(1L);
        verify(notificationRepository).saveAll(unreadNotifications);
    }

    @Test
    void markAllAsRead_whenUserHasNoUnreadNotifications_shouldNotSaveAny() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findUnreadByRecipientId(1L)).thenReturn(List.of());
        when(notificationRepository.saveAll(List.of())).thenReturn(List.of());

        notificationService.markAllAsRead("user1@gmail.com");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(notificationRepository).findUnreadByRecipientId(1L);
        verify(notificationRepository).saveAll(List.of());
    }

    @Test
    void markAllAsRead_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAllAsRead("none@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(notificationRepository, never()).findUnreadByRecipientId(anyLong());
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void createNotification_whenValidUserAndMessage_shouldCreateNotification() {
        String message = "New notification message";

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);

            assertThat(notification.getRecipient()).isEqualTo(testUser);
            assertThat(notification.getMessage()).isEqualTo(message);
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getSentAt()).isNotNull();
            return notification;
        });

        notificationService.createNotification(testUser, message);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotificationByUserId_whenUserExists_shouldCreateNotification() {
        String message = "Notification by user ID";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            assertThat(notification.getRecipient()).isEqualTo(testUser);
            assertThat(notification.getMessage()).isEqualTo(message);
            return notification;
        });

        notificationService.createNotificationByUserId(1L, message);

        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotificationByUserId_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotificationByUserId(999L, "Test message"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}