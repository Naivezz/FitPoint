package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.UserProfileDTO;
import com.naivez.fithub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserProfileDTO toDto(User user);

    User toEntity(UserProfileDTO userProfileDTO);
}
