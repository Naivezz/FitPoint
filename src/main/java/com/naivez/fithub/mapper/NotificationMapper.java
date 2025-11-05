package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.NotificationDTO;
import com.naivez.fithub.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationDTO toDto(Notification notification);

    Notification toEntity(NotificationDTO notificationDTO);
}