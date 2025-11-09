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
    private final RoomRepository roomRepository;

    private final TrainingClassService trainingClassService;

    private final TrainerNoteMapper trainerNoteMapper;
    private final ScheduleChangeRequestMapper scheduleChangeRequestMapper;
    private final UserMapper userMapper;

    public List<TrainingClassDTO> getDailySchedule(String trainerEmail, LocalDate date) {
        return trainingClassService.getDailySchedule(trainerEmail, date);
    }

    public List<TrainingClassDTO> getWeeklySchedule(String trainerEmail, LocalDate startDate) {
        return trainingClassService.getWeeklySchedule(trainerEmail, startDate);
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