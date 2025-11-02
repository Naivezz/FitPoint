package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.MembershipDTO;
import com.naivez.fithub.entity.Membership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MembershipMapper {

    @Mapping(target = "daysRemaining", source = "endDate", qualifiedByName = "calculateDaysRemaining")
    MembershipDTO toDto(Membership membership);

    Membership toEntity(MembershipDTO membershipDTO);

    @Named("calculateDaysRemaining")
    default long calculateDaysRemaining(LocalDate endDate) {
        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(now, endDate);
    }
}
