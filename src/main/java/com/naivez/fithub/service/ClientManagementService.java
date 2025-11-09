package com.naivez.fithub.service;

import com.naivez.fithub.dto.ClientProfileDTO;
import com.naivez.fithub.dto.MembershipTypeDTO;
import com.naivez.fithub.mapper.UserMapper;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<ClientProfileDTO> getAllClients() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "ROLE_CLIENT".equals(role.getName())))
                .map(userMapper::toClientProfileDTO)
                .collect(Collectors.toList());
    }

    public ClientProfileDTO getClientById(Long id) {
        com.naivez.fithub.entity.User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        return userMapper.toClientProfileDTO(user);
    }

    public List<MembershipTypeDTO> getMembershipTypes() {
        return List.of(
                MembershipTypeDTO.builder()
                        .type("MONTHLY")
                        .durationDays(30)
                        .price(new java.math.BigDecimal("99.99"))
                        .build(),
                MembershipTypeDTO.builder()
                        .type("QUARTERLY")
                        .durationDays(90)
                        .price(new java.math.BigDecimal("249.99"))
                        .build(),
                MembershipTypeDTO.builder()
                        .type("ANNUAL")
                        .durationDays(365)
                        .price(new java.math.BigDecimal("899.99"))
                        .build()
        );
    }
}