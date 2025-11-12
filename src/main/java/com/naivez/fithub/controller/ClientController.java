package com.naivez.fithub.controller;

import com.naivez.fithub.dto.*;
import com.naivez.fithub.service.ClientService;
import com.naivez.fithub.service.MembershipService;
import com.naivez.fithub.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {

    private final ReservationService reservationService;
    private final MembershipService membershipService;
    private final ClientService clientService;

    @GetMapping("/classes/available")
    public ResponseEntity<List<TrainingClassDTO>> getAvailableClasses() {
        List<TrainingClassDTO> classes = reservationService.getAvailableClasses();
        return ResponseEntity.ok(classes);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationDTO> createReservation(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ReservationRequest request) {
        ReservationDTO reservation = reservationService.createReservation(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long reservationId) {
        reservationService.cancelReservation(user.getUsername(), reservationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reservations/{reservationId}/rate")
    public ResponseEntity<Void> rateClass(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long reservationId,
            @Valid @RequestBody RatingRequest request) {
        reservationService.rateClass(user.getUsername(), reservationId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @AuthenticationPrincipal UserDetails user) {
        List<ReservationDTO> reservations = reservationService.getMyReservations(user.getUsername());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/reservations/upcoming")
    public ResponseEntity<List<ReservationDTO>> getUpcomingReservations(
            @AuthenticationPrincipal UserDetails user) {
        List<ReservationDTO> reservations = reservationService.getUpcomingReservations(user.getUsername());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/reservations/past")
    public ResponseEntity<List<ReservationDTO>> getPastReservations(
            @AuthenticationPrincipal UserDetails user) {
        List<ReservationDTO> reservations = reservationService.getPastReservations(user.getUsername());
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<MembershipDTO>> getUserMemberships(
            @AuthenticationPrincipal UserDetails user) {
        List<MembershipDTO> memberships = membershipService.getUserMemberships(user.getUsername());
        return ResponseEntity.ok(memberships);
    }

    @GetMapping("/memberships/active")
    public ResponseEntity<List<MembershipDTO>> getActiveMemberships(
            @AuthenticationPrincipal UserDetails user) {
        List<MembershipDTO> memberships = membershipService.getActiveMemberships(user.getUsername());
        return ResponseEntity.ok(memberships);
    }

    @PostMapping("/memberships/purchase")
    public ResponseEntity<MembershipDTO> purchaseMembership(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody PurchaseMembershipRequest request) {
        MembershipDTO membership = membershipService.purchaseMembership(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    @PostMapping("/memberships/topup")
    public ResponseEntity<MembershipDTO> topUpBalance(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody PurchaseMembershipRequest request) {
        MembershipDTO membership = membershipService.topUpBalance(user.getUsername(), request);
        return ResponseEntity.ok(membership);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails user) {
        UserProfileDTO profile = clientService.getProfile(user.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDTO profile = clientService.updateProfile(user.getUsername(), request);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ChangePasswordRequest request) {
        clientService.changePassword(user.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
