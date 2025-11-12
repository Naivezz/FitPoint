package com.naivez.fithub.service;

import com.naivez.fithub.dto.PersonalTrainingSessionDTO;
import com.naivez.fithub.dto.PersonalTrainingSessionRequest;
import com.naivez.fithub.entity.PersonalTrainingSession;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.PersonalTrainingSessionMapper;
import com.naivez.fithub.repository.PersonalTrainingSessionRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalTrainingSessionService {

    private final PersonalTrainingSessionRepository personalTrainingSessionRepository;
    private final UserRepository userRepository;
    private final PersonalTrainingSessionMapper personalTrainingSessionMapper;
    private final NotificationService notificationService;

    @Transactional
    public PersonalTrainingSessionDTO createPersonalSession(String trainerEmail, PersonalTrainingSessionRequest request) {
        log.info("Creating personal session - trainer: {}, clientId: {}, startTime: {}",
                trainerEmail, request.getClientId(), request.getStartTime());

        User trainer = getTrainerByEmail(trainerEmail);

        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (request.getStartTime().isAfter(request.getEndTime())) {
            log.warn("Session creation failed - start time after end time: {}", request.getStartTime());
            throw new RuntimeException("Start time must be before end time");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            log.warn("Session creation failed - start time in past: {}", request.getStartTime());
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

        log.info("Personal session created successfully - id: {}, trainer: {}, client: {}",
                session.getId(), trainerEmail, client.getEmail());

        return personalTrainingSessionMapper.toDto(session);
    }

    @Transactional
    public PersonalTrainingSessionDTO updatePersonalSession(String trainerEmail, Long sessionId, PersonalTrainingSessionRequest request) {
        log.info("Updating personal session - trainer: {}, sessionId: {}", trainerEmail, sessionId);

        User trainer = getTrainerByEmail(trainerEmail);

        PersonalTrainingSession session = personalTrainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTrainer().getId().equals(trainer.getId())) {
            log.warn("Session update failed - trainer mismatch: {} vs {}", trainerEmail, session.getTrainer().getEmail());
            throw new RuntimeException("You can only update your own sessions");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            log.warn("Session update failed - start time after end time: {}", request.getStartTime());
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

        log.info("Personal session updated successfully - id: {}, trainer: {}", sessionId, trainerEmail);

        return personalTrainingSessionMapper.toDto(session);
    }

    @Transactional
    public void cancelPersonalSession(String trainerEmail, Long sessionId) {
        log.info("Canceling personal session - trainer: {}, sessionId: {}", trainerEmail, sessionId);

        User trainer = getTrainerByEmail(trainerEmail);

        PersonalTrainingSession session = personalTrainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTrainer().getId().equals(trainer.getId())) {
            log.warn("Session cancellation failed - trainer mismatch: {} vs {}", trainerEmail, session.getTrainer().getEmail());
            throw new RuntimeException("You can only cancel your own sessions");
        }

        session.setStatus("CANCELLED");
        session.setUpdatedAt(LocalDateTime.now());
        personalTrainingSessionRepository.save(session);

        notificationService.createNotification(session.getClient(),
                "Personal training session cancelled for " + session.getStartTime());

        log.info("Personal session cancelled successfully - id: {}, trainer: {}", sessionId, trainerEmail);
    }

    @Transactional
    public PersonalTrainingSessionDTO completePersonalSession(String trainerEmail, Long sessionId, String sessionNotes) {
        log.info("Completing personal session - trainer: {}, sessionId: {}", trainerEmail, sessionId);

        User trainer = getTrainerByEmail(trainerEmail);

        PersonalTrainingSession session = personalTrainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTrainer().getId().equals(trainer.getId())) {
            log.warn("Session completion failed - trainer mismatch: {} vs {}", trainerEmail, session.getTrainer().getEmail());
            throw new RuntimeException("You can only complete your own sessions");
        }

        session.setStatus("COMPLETED");
        if (sessionNotes != null) {
            session.setSessionNotes(sessionNotes);
        }
        session.setUpdatedAt(LocalDateTime.now());

        session = personalTrainingSessionRepository.save(session);
        log.info("Personal session completed successfully - id: {}, trainer: {}", sessionId, trainerEmail);

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

    public List<PersonalTrainingSessionDTO> getPersonalSessionsInRange(String trainerEmail,
                                                                       java.time.LocalDate startDate,
                                                                       java.time.LocalDate endDate) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(java.time.LocalTime.MAX);

        List<PersonalTrainingSession> sessions = personalTrainingSessionRepository
                .findByTrainerIdAndDateRange(trainer.getId(), start, end);

        return sessions.stream()
                .map(personalTrainingSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getTrainerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isTrainer = user.getRoles().stream()
                .anyMatch(role -> "ROLE_TRAINER".equals(role.getName()));

        if (!isTrainer) {
            log.warn("User is not a trainer: {}", email);
            throw new RuntimeException("User is not a trainer");
        }

        return user;
    }
}