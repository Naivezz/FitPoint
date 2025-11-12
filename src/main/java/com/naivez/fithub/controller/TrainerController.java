package com.naivez.fithub.controller;

import com.naivez.fithub.dto.*;
import com.naivez.fithub.service.NotificationService;
import com.naivez.fithub.service.PersonalTrainingSessionService;
import com.naivez.fithub.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class TrainerController {

    private final TrainerService trainerService;
    private final PersonalTrainingSessionService personalTrainingSessionService;
    private final NotificationService notificationService;

    @GetMapping("/schedule/daily")
    public ResponseEntity<List<TrainingClassDTO>> getDailySchedule(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TrainingClassDTO> schedule = trainerService.getDailySchedule(user.getUsername(), date);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/schedule/weekly")
    public ResponseEntity<List<TrainingClassDTO>> getWeeklySchedule(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        List<TrainingClassDTO> schedule = trainerService.getWeeklySchedule(user.getUsername(), startDate);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/schedule/personal-sessions")
    public ResponseEntity<List<PersonalTrainingSessionDTO>> getPersonalSessionsInRange(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PersonalTrainingSessionDTO> sessions = personalTrainingSessionService.getPersonalSessionsInRange(
                user.getUsername(), startDate, endDate);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/schedule/change-request")
    public ResponseEntity<ScheduleChangeRequestDTO> createScheduleChangeRequest(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ScheduleChangeRequestRequest request) {
        ScheduleChangeRequestDTO result = trainerService.createScheduleChangeRequest(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/schedule/change-requests")
    public ResponseEntity<List<ScheduleChangeRequestDTO>> getScheduleChangeRequests(
            @AuthenticationPrincipal UserDetails user) {
        List<ScheduleChangeRequestDTO> requests = trainerService.getScheduleChangeRequests(user.getUsername());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/schedule/change-requests/pending")
    public ResponseEntity<List<ScheduleChangeRequestDTO>> getPendingScheduleChangeRequests(
            @AuthenticationPrincipal UserDetails user) {
        List<ScheduleChangeRequestDTO> requests = trainerService.getPendingScheduleChangeRequests(user.getUsername());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientProfileDTO>> getClients(
            @AuthenticationPrincipal UserDetails user) {
        List<ClientProfileDTO> clients = trainerService.getTrainerClients(user.getUsername());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientProfileDTO> getClientProfile(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long clientId) {
        ClientProfileDTO client = trainerService.getClientProfile(user.getUsername(), clientId);
        return ResponseEntity.ok(client);
    }

    @PostMapping("/notes")
    public ResponseEntity<TrainerNoteDTO> addClientNote(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody TrainerNoteRequest request) {
        TrainerNoteDTO note = trainerService.addClientNote(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<TrainerNoteDTO> updateClientNote(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long noteId,
            @Valid @RequestBody TrainerNoteRequest request) {
        TrainerNoteDTO note = trainerService.updateClientNote(user.getUsername(), noteId, request);
        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteClientNote(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long noteId) {
        trainerService.deleteClientNote(user.getUsername(), noteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notes")
    public ResponseEntity<List<TrainerNoteDTO>> getAllNotes(
            @AuthenticationPrincipal UserDetails user) {
        List<TrainerNoteDTO> notes = trainerService.getAllNotes(user.getUsername());
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/notes/client/{clientId}")
    public ResponseEntity<List<TrainerNoteDTO>> getClientNotes(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long clientId) {
        List<TrainerNoteDTO> notes = trainerService.getClientNotes(user.getUsername(), clientId);
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/personal-sessions")
    public ResponseEntity<PersonalTrainingSessionDTO> createPersonalSession(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody PersonalTrainingSessionRequest request) {
        PersonalTrainingSessionDTO session = personalTrainingSessionService.createPersonalSession(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PutMapping("/personal-sessions/{sessionId}")
    public ResponseEntity<PersonalTrainingSessionDTO> updatePersonalSession(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long sessionId,
            @Valid @RequestBody PersonalTrainingSessionRequest request) {
        PersonalTrainingSessionDTO session = personalTrainingSessionService.updatePersonalSession(user.getUsername(), sessionId, request);
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/personal-sessions/{sessionId}")
    public ResponseEntity<Void> cancelPersonalSession(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long sessionId) {
        personalTrainingSessionService.cancelPersonalSession(user.getUsername(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/personal-sessions/{sessionId}/complete")
    public ResponseEntity<PersonalTrainingSessionDTO> completePersonalSession(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long sessionId,
            @RequestParam(required = false) String sessionNotes) {
        PersonalTrainingSessionDTO session = personalTrainingSessionService.completePersonalSession(user.getUsername(), sessionId, sessionNotes);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/personal-sessions")
    public ResponseEntity<List<PersonalTrainingSessionDTO>> getAllPersonalSessions(
            @AuthenticationPrincipal UserDetails user) {
        List<PersonalTrainingSessionDTO> sessions = personalTrainingSessionService.getAllPersonalSessions(user.getUsername());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/personal-sessions/client/{clientId}")
    public ResponseEntity<List<PersonalTrainingSessionDTO>> getClientPersonalSessions(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long clientId) {
        List<PersonalTrainingSessionDTO> sessions =
                personalTrainingSessionService.getClientPersonalSessions(user.getUsername(), clientId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/classes/{classId}/registrations")
    public ResponseEntity<List<UserProfileDTO>> getClassRegistrations(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long classId) {
        List<UserProfileDTO> registrations = trainerService.getClassRegistrations(user.getUsername(), classId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal UserDetails user) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(user.getUsername());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails user) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getUsername());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(user.getUsername(), notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @AuthenticationPrincipal UserDetails user) {
        notificationService.markAllAsRead(user.getUsername());
        return ResponseEntity.ok().build();
    }
}
