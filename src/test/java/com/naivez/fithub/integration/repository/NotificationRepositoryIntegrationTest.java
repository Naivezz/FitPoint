package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Notification;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;
    private User otherUser;
    private Notification readNotification;
    private Notification unreadNotification1;
    private Notification unreadNotification2;
    private Notification otherUserNotification;

    @BeforeEach
    void setUp() {
        Role clientRole = Role.builder()
                .name("ROLE_CLIENT")
                .users(new HashSet<>())
                .build();
        entityManager.persist(clientRole);

        testUser = User.builder()
                .email("user1@gmail.com")
                .password("encodedPassword")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        testUser.getRoles().add(clientRole);
        entityManager.persist(testUser);

        otherUser = User.builder()
                .email("other@gmail.com")
                .password("encodedPassword")
                .firstName("user2")
                .lastName("user2")
                .phone("2222222222")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        otherUser.getRoles().add(clientRole);
        entityManager.persist(otherUser);

        readNotification = Notification.builder()
                .recipient(testUser)
                .message("This notification has been read")
                .sentAt(LocalDateTime.now().minusDays(2))
                .read(true)
                .build();

        unreadNotification1 = Notification.builder()
                .recipient(testUser)
                .message("First unread notification")
                .sentAt(LocalDateTime.now().minusDays(1))
                .read(false)
                .build();

        unreadNotification2 = Notification.builder()
                .recipient(testUser)
                .message("Second unread notification")
                .sentAt(LocalDateTime.now().minusHours(1))
                .read(false)
                .build();

        otherUserNotification = Notification.builder()
                .recipient(otherUser)
                .message("Notification for other user")
                .sentAt(LocalDateTime.now().minusHours(2))
                .read(false)
                .build();

        entityManager.persist(readNotification);
        entityManager.persist(unreadNotification1);
        entityManager.persist(unreadNotification2);
        entityManager.persist(otherUserNotification);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByRecipientId_shouldReturnNotificationsOrderedBySentAtDesc() {
        List<Notification> result = notificationRepository.findByRecipientId(testUser.getId());

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getMessage()).isEqualTo("Second unread notification");
        assertThat(result.get(1).getMessage()).isEqualTo("First unread notification");
        assertThat(result.get(2).getMessage()).isEqualTo("This notification has been read");

        for (Notification notification : result) {
            assertThat(notification.getRecipient().getId()).isEqualTo(testUser.getId());
        }
    }

    @Test
    void findByRecipientId_whenUserHasNoNotifications_shouldReturnEmptyList() {
        User userWithoutNotifications = User.builder()
                .email("nonotifications@gmail.com")
                .password("encodedPassword")
                .firstName("Empty")
                .lastName("User")
                .phone("5555555555")
                .roles(new HashSet<>())
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();
        entityManager.persistAndFlush(userWithoutNotifications);

        List<Notification> result = notificationRepository.findByRecipientId(userWithoutNotifications.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findUnreadByRecipientId_shouldReturnOnlyUnreadNotifications() {
        List<Notification> result = notificationRepository.findUnreadByRecipientId(testUser.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Notification::getMessage)
                .containsExactly("Second unread notification", "First unread notification");

        for (Notification notification : result) {
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getRecipient().getId()).isEqualTo(testUser.getId());
        }
    }

    @Test
    void findUnreadByRecipientId_shouldBeOrderedBySentAtDesc() {
        List<Notification> result = notificationRepository.findUnreadByRecipientId(testUser.getId());

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getSentAt()).isAfter(result.get(1).getSentAt());
        assertThat(result.get(0).getMessage()).isEqualTo("Second unread notification");
        assertThat(result.get(1).getMessage()).isEqualTo("First unread notification");
    }

    @Test
    void findUnreadByRecipientId_whenAllNotificationsAreRead_shouldReturnEmptyList() {
        List<Notification> userNotifications = notificationRepository.findByRecipientId(testUser.getId());
        for (Notification notification : userNotifications) {
            notification.setRead(true);
            entityManager.merge(notification);
        }
        entityManager.flush();
        entityManager.clear();

        List<Notification> result = notificationRepository.findUnreadByRecipientId(testUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void update_existingNotification_shouldModifyAndPersist() {
        Long notificationId = unreadNotification1.getId();

        Notification notificationToUpdate = notificationRepository.findById(notificationId).orElseThrow();
        notificationToUpdate.setRead(true);
        notificationToUpdate.setMessage("Updated message");
        notificationRepository.save(notificationToUpdate);
        entityManager.flush();
        entityManager.clear();

        Notification verifyNotification = entityManager.find(Notification.class, notificationId);
        assertThat(verifyNotification.isRead()).isTrue();
        assertThat(verifyNotification.getMessage()).isEqualTo("Updated message");
    }

    @Test
    void findByRecipientId_shouldNotReturnOtherUsersNotifications() {
        List<Notification> result = notificationRepository.findByRecipientId(testUser.getId());

        assertThat(result).hasSize(3);

        assertThat(result).extracting(notification -> notification.getRecipient().getId())
                .allMatch(recipientId -> recipientId.equals(testUser.getId()));
        assertThat(result).extracting(Notification::getMessage)
                .doesNotContain("Notification for other user");
    }
}