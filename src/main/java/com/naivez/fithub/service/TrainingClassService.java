package com.naivez.fithub.service;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.dto.TrainingClassRequest;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.TrainingClassMapper;
import com.naivez.fithub.repository.RoomRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingClassService {

    private final TrainingClassRepository trainingClassRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final TrainingClassMapper trainingClassMapper;

    public List<TrainingClassDTO> getAllTrainingClasses() {
        return trainingClassRepository.findAll().stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TrainingClassDTO> getUpcomingClasses() {
        return trainingClassRepository.findUpcomingClasses(LocalDateTime.now()).stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TrainingClassDTO> getClassesBetween(LocalDateTime start, LocalDateTime end) {
        return trainingClassRepository.findClassesBetween(start, end).stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TrainingClassDTO> getDailySchedule(String trainerEmail, LocalDate date) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<TrainingClass> classes = trainingClassRepository.findByTrainerIdAndDateRange(
                trainer.getId(), start, end);

        return classes.stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TrainingClassDTO> getWeeklySchedule(String trainerEmail, LocalDate startDate) {
        User trainer = getTrainerByEmail(trainerEmail);

        LocalDate endDate = startDate.plusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<TrainingClass> classes = trainingClassRepository.findByTrainerIdAndDateRange(
                trainer.getId(), start, end);

        return classes.stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingClassDTO createTrainingClass(TrainingClassRequest request) {
        log.info("Creating training class - name: {}, trainerId: {}, roomId: {}, start: {}",
                request.getName(), request.getTrainerId(), request.getRoomId(), request.getStartTime());

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> {
                    log.error("Trainer not found for class creation - trainerId: {}", request.getTrainerId());
                    return new RuntimeException("Trainer not found with id: " + request.getTrainerId());
                });

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> {
                    log.error("Room not found for class creation - roomId: {}", request.getRoomId());
                    return new RuntimeException("Room not found with id: " + request.getRoomId());
                });

        if (request.getEndTime().isBefore(request.getStartTime())) {
            log.warn("Class creation failed - end time before start time: {} to {}",
                    request.getStartTime(), request.getEndTime());
            throw new RuntimeException("End time must be after start time");
        }

        TrainingClass trainingClass = trainingClassMapper.toEntity(request);
        trainingClass.setTrainer(trainer);
        trainingClass.setRoom(room);

        if (trainingClass.getReservations() == null) {
            trainingClass.setReservations(new HashSet<>());
        }

        trainingClass = trainingClassRepository.save(trainingClass);
        log.info("Training class created successfully - id: {}, name: {}, trainer: {}, room: {}",
                trainingClass.getId(), trainingClass.getName(), trainer.getEmail(), room.getName());

        return trainingClassMapper.toDto(trainingClass);
    }

    @Transactional
    public TrainingClassDTO updateTrainingClass(Long id, TrainingClassRequest request) {
        log.info("Updating training class - id: {}, new name: {}, trainerId: {}",
                id, request.getName(), request.getTrainerId());

        TrainingClass trainingClass = trainingClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training class not found with id: " + id));

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> {
                    log.error("Trainer not found for class update - trainerId: {}", request.getTrainerId());
                    return new RuntimeException("Trainer not found with id: " + request.getTrainerId());
                });

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> {
                    log.error("Room not found for class update - roomId: {}", request.getRoomId());
                    return new RuntimeException("Room not found with id: " + request.getRoomId());
                });

        if (request.getEndTime().isBefore(request.getStartTime())) {
            log.warn("Class update failed - end time before start time: {} to {}",
                    request.getStartTime(), request.getEndTime());
            throw new RuntimeException("End time must be after start time");
        }

        trainingClassMapper.updateFromRequest(request, trainingClass);
        trainingClass.setTrainer(trainer);
        trainingClass.setRoom(room);

        trainingClass = trainingClassRepository.save(trainingClass);
        log.info("Training class updated successfully - id: {}, name: {}", id, trainingClass.getName());

        return trainingClassMapper.toDto(trainingClass);
    }

    @Transactional
    public void deleteTrainingClass(Long id) {
        log.info("Deleting training class - id: {}", id);
        if (!trainingClassRepository.existsById(id)) {
            throw new RuntimeException("Training class not found with id: " + id);
        }
        trainingClassRepository.deleteById(id);
        log.info("Training class deleted successfully - id: {}", id);
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