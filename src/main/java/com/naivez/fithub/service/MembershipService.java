package com.naivez.fithub.service;

import com.naivez.fithub.dto.MembershipDTO;
import com.naivez.fithub.dto.PurchaseMembershipRequest;
import com.naivez.fithub.entity.Membership;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.MembershipMapper;
import com.naivez.fithub.repository.MembershipRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipMapper membershipMapper;

    private static final Map<String, MembershipConfig> MEMBERSHIP_TYPES = new HashMap<>();

    static {
        MEMBERSHIP_TYPES.put("MONTHLY", new MembershipConfig(30, new BigDecimal("99.99")));
        MEMBERSHIP_TYPES.put("QUARTERLY", new MembershipConfig(90, new BigDecimal("249.99")));
        MEMBERSHIP_TYPES.put("ANNUAL", new MembershipConfig(365, new BigDecimal("899.99")));
    }

    public List<MembershipDTO> getUserMemberships(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Membership> memberships = membershipRepository.findByUserIdOrderByEndDateDesc(user.getId());

        return memberships.stream()
                .map(membershipMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<MembershipDTO> getActiveMemberships(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        List<Membership> memberships = membershipRepository.findActiveByUserId(user.getId(), today);

        return memberships.stream()
                .map(membershipMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MembershipDTO purchaseMembership(String userEmail, PurchaseMembershipRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String type = request.getType().toUpperCase();
        if (!MEMBERSHIP_TYPES.containsKey(type)) {
            throw new RuntimeException("Invalid membership type. Valid types: MONTHLY, QUARTERLY, ANNUAL");
        }

        MembershipConfig config = MEMBERSHIP_TYPES.get(type);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(config.durationDays);

        Membership membership = Membership.builder()
                .user(user)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .price(config.price)
                .active(true)
                .build();

        membership = membershipRepository.save(membership);

        return membershipMapper.toDto(membership);
    }

    @Transactional
    public MembershipDTO topUpBalance(String userEmail, PurchaseMembershipRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String type = request.getType().toUpperCase();
        if (!MEMBERSHIP_TYPES.containsKey(type)) {
            throw new RuntimeException("Invalid membership type. Valid types: MONTHLY, QUARTERLY, ANNUAL");
        }

        MembershipConfig config = MEMBERSHIP_TYPES.get(type);

        LocalDate today = LocalDate.now();
        List<Membership> validMemberships = membershipRepository.findValidByUserId(user.getId(), today);

        if (validMemberships.isEmpty()) {
            return purchaseMembership(userEmail, request);
        }

        Membership membership = validMemberships.get(0);
        LocalDate newEndDate = membership.getEndDate().plusDays(config.durationDays);
        membership.setEndDate(newEndDate);

        membership = membershipRepository.save(membership);

        return membershipMapper.toDto(membership);
    }

    public boolean hasActiveMembership(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        List<Membership> activeMemberships = membershipRepository.findActiveByUserId(user.getId(), today);

        return !activeMemberships.isEmpty();
    }

    private static class MembershipConfig {
        int durationDays;
        BigDecimal price;

        MembershipConfig(int durationDays, BigDecimal price) {
            this.durationDays = durationDays;
            this.price = price;
        }
    }
}
