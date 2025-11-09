package com.naivez.fithub.controller;

import com.naivez.fithub.dto.ClientProfileDTO;
import com.naivez.fithub.dto.MembershipTypeDTO;
import com.naivez.fithub.service.ClientManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClientManagementController {

    private final ClientManagementService clientManagementService;

    @GetMapping
    public ResponseEntity<List<ClientProfileDTO>> getAllClients() {
        List<ClientProfileDTO> clients = clientManagementService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientProfileDTO> getClientById(@PathVariable Long id) {
        try {
            ClientProfileDTO client = clientManagementService.getClientById(id);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/membership-types")
    public ResponseEntity<List<MembershipTypeDTO>> getMembershipTypes() {
        List<MembershipTypeDTO> membershipTypes = clientManagementService.getMembershipTypes();
        return ResponseEntity.ok(membershipTypes);
    }
}