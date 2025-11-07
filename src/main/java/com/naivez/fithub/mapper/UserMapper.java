package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.ClientProfileDTO;
import com.naivez.fithub.dto.EmployeeDTO;
import com.naivez.fithub.dto.UserProfileDTO;
import com.naivez.fithub.entity.Role;
import com.naivez.fithub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserProfileDTO toDto(User user);

    User toEntity(UserProfileDTO userProfileDTO);

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    EmployeeDTO toEmployeeDTO(User user);

    @Mapping(target = "id", source = "id")
    ClientProfileDTO toClientProfileDTO(User user);

    @Mapping(target = "id", source = "id")
    ClientProfileDTO toClientProfileDTO(UserProfileDTO userProfile);

    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
