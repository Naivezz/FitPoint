package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.NotificationDTO;
import com.naivez.fithub.entity.Notification;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.NotificationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private NotificationMapper notificationMapper;

    @BeforeEach
    void setUp() {
        notificationMapper = Mappers.getMapper(NotificationMapper.class);
    }

    @Test
    void toDto_whenEntityHasBasicFields_shouldMapAllFields() {
        User recipient = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .build();

        LocalDateTime sentTime = LocalDateTime.of(2025, 10, 30, 14, 30, 0);

        Notification notification = Notification.builder()
                .id(1L)
                .recipient(recipient)
                .message("Some message")
                .sentAt(sentTime)
                .read(false)
                .build();

        NotificationDTO result = notificationMapper.toDto(notification);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessage()).isEqualTo("Some message");
        assertThat(result.getSentAt()).isEqualTo(sentTime);
        assertThat(result.isRead()).isFalse();
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        NotificationDTO result = notificationMapper.toDto((Notification) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        LocalDateTime sentTime = LocalDateTime.of(2025, 11, 15, 9, 0, 0);

        NotificationDTO dto = NotificationDTO.builder()
                .id(1L)
                .message("message")
                .sentAt(sentTime)
                .read(true)
                .build();

        Notification result = notificationMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessage()).isEqualTo("message");
        assertThat(result.getSentAt()).isEqualTo(sentTime);
        assertThat(result.isRead()).isTrue();
        assertThat(result.getRecipient()).isNull();
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Notification result = notificationMapper.toEntity((NotificationDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_withNullRecipient_shouldMapOtherFields() {
        Notification notification = Notification.builder()
                .id(9L)
                .recipient(null)
                .message("message")
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        NotificationDTO result = notificationMapper.toDto(notification);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(9L);
        assertThat(result.getMessage()).isEqualTo("message");
        assertThat(result.isRead()).isFalse();
    }
}