package com.naivez.fithub.service;

import com.naivez.fithub.dto.*;
import com.naivez.fithub.entity.*;
import com.naivez.fithub.mapper.*;
import com.naivez.fithub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerService {

    private final TrainingClassRepository trainingClassRepository;
    private final UserRepository userRepository;
    private final TrainerNoteRepository trainerNoteRepository;
    private final ScheduleChangeRequestRepository scheduleChangeRequestRepository;
    private final PersonalTrainingSessionRepository personalTrainingSessionRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    private final TrainingClassMapper trainingClassMapper;
    private final TrainerNoteMapper trainerNoteMapper;
    private final ScheduleChangeRequestMapper scheduleChangeRequestMapper;
    private final PersonalTrainingSessionMapper personalTrainingSessionMapper;
    private final UserMapper userMapper;

    public List<TrainingClassDTO> getDailySchedule(String trainerEmail, LocalDate date) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<TrainingClass> classes = trainingClassRepository.findByTrainerIdAndDateRange(
                trainer.getId(), startOfDay, endOfDay);

        return classes.stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TrainingClassDTO> getWeeklySchedule(String trainerEmail, LocalDate startDate) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDateTime startOfWeek = startDate.atStartOfDay();
        LocalDateTime endOfWeek = startDate.plusDays(6).atTime(LocalTime.MAX);

        List<TrainingClass> classes = trainingClassRepository.findByTrainerIdAndDateRange(
                trainer.getId(), startOfWeek, endOfWeek);

        return classes.stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PersonalTrainingSessionDTO> getPersonalSessionsInRange(String trainerEmail, LocalDate startDate, LocalDate endDate) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository.findByTrainerIdAndDateRange(
                trainer.getId(), start, end);

        return sessions.stream()
                .map(personalTrainingSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleChangeRequestDTO createScheduleChangeRequest(String trainerEmail, ScheduleChangeRequestRequest request) {
        User trainer = getTrainerByEmail(trainerEmail);

        if (!List.of("ADD", "MODIFY", "CANCEL").contains(request.getRequestType())) {
            throw new RuntimeException("Invalid request type. Must be ADD, MODIFY, or CANCEL");
        }

        ScheduleChangeRequest.ScheduleChangeRequestBuilder builder = ScheduleChangeRequest.builder()
                .trainer(trainer)
                .requestType(request.getRequestType())
                .reason(request.getReason())
                .status("PENDING")
                .createdAt(LocalDateTime.now());

        if (("MODIFY".equals(request.getRequestType()) || "CANCEL".equals(request.getRequestType()))
                && request.getTrainingClassId() != null) {
            TrainingClass trainingClass = trainingClassRepository.findById(request.getTrainingClassId())
                    .orElseThrow(() -> new RuntimeException("Training class not found"));

            if (!trainingClass.getTrainer().getId().equals(trainer.getId())) {
                throw new RuntimeException("You can only modify or cancel your own classes");
            }

            builder.trainingClass(trainingClass);
        }

        if ("ADD".equals(request.getRequestType()) || "MODIFY".equals(request.getRequestType())) {
            builder.className(request.getClassName())
                    .classDescription(request.getClassDescription())
                    .requestedStartTime(request.getRequestedStartTime())
                    .requestedEndTime(request.getRequestedEndTime())
                    .requestedCapacity(request.getRequestedCapacity());

            if (request.getRequestedRoomId() != null) {
                Room room = roomRepository.findById(request.getRequestedRoomId())
                        .orElseThrow(() -> new RuntimeException("Room not found"));
                builder.requestedRoom(room);
            }
        }

        ScheduleChangeRequest scheduleChangeRequest = builder.build();
        scheduleChangeRequest = scheduleChangeRequestRepository.save(scheduleChangeRequest);

        return scheduleChangeRequestMapper.toDto(scheduleChangeRequest);
    }

    public List<ScheduleChangeRequestDTO> getScheduleChangeRequests(String trainerEmail) {
        User trainer = getTrainerByEmail(trainerEmail);

        List<ScheduleChangeRequest> requests = scheduleChangeRequestRepository.findByTrainerId(trainer.getId());

        return requests.stream()
                .map(scheduleChangeRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ScheduleChangeRequestDTO> getPendingScheduleChangeRequests(String trainerEmail) {
        User trainer = getTrainerByEmail(trainerEmail);

        List<ScheduleChangeRequest> requests = scheduleChangeRequestRepository.findByTrainerIdAndStatus(
                trainer.getId(), "PENDING");

        return requests.stream()
                .map(scheduleChangeRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ClientProfileDTO> getTrainerClients(String trainerEmail) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDateTime now = LocalDateTime.now();
        List<TrainingClass> trainerClasses = trainingClassRepository.findByTrainerIdAndDateRange(
                trainer.getId(), now.minusMonths(6), now.plusMonths(6));

        List<User> clients = trainerClasses.stream()
                .flatMap(tc -> tc.getReservations().stream())
                .map(Reservation::getUser)
                .distinct()
                .collect(Collectors.toList());

        return clients.stream()
                .map(userMapper::toClientProfileDTO)
                .collect(Collectors.toList());
    }

    public ClientProfileDTO getClientProfile(String trainerEmail, Long clientId) {
        User trainer = getTrainerByEmail(trainerEmail);

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (!hasAccessToClient(trainer.getId(), clientId)) {
            throw new RuntimeException("You don't have access to this client's profile");
        }

        return userMapper.toClientProfileDTO(client);
    }

    @Transactional
    public TrainerNoteDTO addClientNote(String trainerEmail, TrainerNoteRequest request) {
        User trainer = getTrainerByEmail(trainerEmail);

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (!hasAccessToClient(trainer.getId(), request.getClientId())) {
            throw new RuntimeException("You don't have access to this client");
        }

        TrainerNote note = TrainerNote.builder()
                .trainer(trainer)
                .client(client)
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        note = trainerNoteRepository.save(note);

        return trainerNoteMapper.toDto(note);
    }

    @Transactional
    public TrainerNoteDTO updateClientNote(String trainerEmail, Long noteId, TrainerNoteRequest request) {
        User trainer = getTrainerByEmail(trainerEmail);

        TrainerNote note = trainerNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("You can only update your own notes");
        }

        note.setNote(request.getNote());
        note.setUpdatedAt(LocalDateTime.now());

        note = trainerNoteRepository.save(note);

        return trainerNoteMapper.toDto(note);
    }

    @Transactional
    public void deleteClientNote(String trainerEmail, Long noteId) {
        User trainer = getTrainerByEmail(trainerEmail);

        TrainerNote note = trainerNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("You can only delete your own notes");
        }

        trainerNoteRepository.delete(note);
    }

    public List<TrainerNoteDTO> getAllNotes(String trainerEmail) {
        User trainer = getTrainerByEmail(trainerEmail);

        List<TrainerNote> notes = trainerNoteRepository.findByTrainerId(trainer.getId());

        return notes.stream()
                .map(trainerNoteMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TrainerNoteDTO> getClientNotes(String trainerEmail, Long clientId) {
        User trainer = getTrainerByEmail(trainerEmail);

        if (!hasAccessToClient(trainer.getId(), clientId)) {
            throw new RuntimeException("You don't have access to this client");
        }

        List<TrainerNote> notes = trainerNoteRepository.findByTrainerIdAndClientId(trainer.getId(), clientId);

        return notes.stream()
                .map(trainerNoteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalTrainingSessionDTO createPersonalSession(String trainerEmail, PersonalTrainingSessionRequest request) {
        User trainer = getTrainerByEmail(trainerEmail);

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot schedule session in the past");
        }

        PersonalTrainingSession session = PersonalTrainingSession.builder()
                .trainer(trainer)
                .client(client)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .sessionGoal(request.getSessionGoal())
                .sessionNotes(request.getSessionNotes())
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();

        session = personalTrainingSessionRepository.save(session);

        notificationService.createNotification(client,
                "New personal training session scheduled with " + trainer.getFirstName() + " " + trainer.getLastName() +
                        " on " + session.getStartTime());

        return personalTrainingSessionMapper.toDto(session);
    }

    @Transactional
    public PersonalTrainingSessionDTO updatePersonalSession(String trainerEmail, Long sessionId, PersonalTrainingSessionRequest request) {
        User trainer = getTrainerByEmail(trainerEmail);

        PersonalTrainingSession session = personalTrainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("You can only update your own sessions");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setSessionGoal(request.getSessionGoal());
        session.setSessionNotes(request.getSessionNotes());
        session.setUpdatedAt(LocalDateTime.now());

        session = personalTrainingSessionRepository.save(session);

        notificationService.createNotification(session.getClient(),
                "Personal training session updated for " + session.getStartTime());

        return personalTrainingSessionMapper.toDto(session);
    }

    @Transactional
    public void cancelPersonalSession(String trainerEmail, Long sessionId) {
        User trainer = getTrainerByEmail(trainerEmail);

        PersonalTrainingSession session = personalTrainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("You can only cancel your own sessions");
        }

        session.setStatus("CANCELLED");
        session.setUpdatedAt(LocalDateTime.now());
        personalTrainingSessionRepository.save(session);

        notificationService.createNotification(session.getClient(),
                "Personal training session cancelled for " + session.getStartTime());
    }

    @Transactional
    public PersonalTrainingSessionDTO completePersonalSession(String trainerEmail, Long sessionId, String sessionNotes) {
        User trainer = getTrainerByEmail(trainerEmail);

        PersonalTrainingSession session = personalTrainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("You can only complete your own sessions");
        }

        session.setStatus("COMPLETED");
        if (sessionNotes != null) {
            session.setSessionNotes(sessionNotes);
        }
        session.setUpdatedAt(LocalDateTime.now());

        session = personalTrainingSessionRepository.save(session);

        return personalTrainingSessionMapper.toDto(session);
    }

    public List<PersonalTrainingSessionDTO> getAllPersonalSessions(String trainerEmail) {
        User trainer = getTrainerByEmail(trainerEmail);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository.findByTrainerId(trainer.getId());

        return sessions.stream()
                .map(personalTrainingSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PersonalTrainingSessionDTO> getClientPersonalSessions(String trainerEmail, Long clientId) {
        User trainer = getTrainerByEmail(trainerEmail);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository.findByTrainerIdAndClientId(
                trainer.getId(), clientId);

        return sessions.stream()
                .map(personalTrainingSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserProfileDTO> getClassRegistrations(String trainerEmail, Long classId) {
        User trainer = getTrainerByEmail(trainerEmail);

        TrainingClass trainingClass = trainingClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Training class not found"));

        if (!trainingClass.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("You can only view registrations for your own classes");
        }

        return trainingClass.getReservations().stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .map(Reservation::getUser)
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getTrainerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isTrainer = user.getRoles().stream()
                .anyMatch(role -> "ROLE_TRAINER".equals(role.getName()));

        if (!isTrainer) {
            throw new RuntimeException("User is not a trainer");
        }

        return user;
    }

    private boolean hasAccessToClient(Long trainerId, Long clientId) {
        LocalDateTime now = LocalDateTime.now();
        List<TrainingClass> trainerClasses = trainingClassRepository.findByTrainerIdAndDateRange(
                trainerId, now.minusMonths(6), now.plusMonths(6));

        return trainerClasses.stream()
                .flatMap(tc -> tc.getReservations().stream())
                .anyMatch(r -> r.getUser().getId().equals(clientId));
    }
}