package com.naivez.fithub.service;

import com.naivez.fithub.dto.ReviewScheduleChangeRequest;
import com.naivez.fithub.dto.ScheduleChangeRequestDTO;
import com.naivez.fithub.entity.ScheduleChangeRequest;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.ScheduleChangeRequestMapper;
import com.naivez.fithub.repository.ScheduleChangeRequestRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleChangeRequestService {

    private final ScheduleChangeRequestRepository scheduleChangeRequestRepository;
    private final UserRepository userRepository;
    private final TrainingClassRepository trainingClassRepository;
    private final ScheduleChangeRequestMapper scheduleChangeRequestMapper;

    public List<ScheduleChangeRequestDTO> getAllScheduleChangeRequests() {
        return scheduleChangeRequestRepository.findAll().stream()
                .map(scheduleChangeRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ScheduleChangeRequestDTO> getPendingScheduleChangeRequests() {
        return scheduleChangeRequestRepository.findByStatus("PENDING").stream()
                .map(scheduleChangeRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public ScheduleChangeRequestDTO getScheduleChangeRequestById(Long id) {
        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule change request not found with id: " + id));
        return scheduleChangeRequestMapper.toDto(request);
    }

    @Transactional
    public ScheduleChangeRequestDTO reviewScheduleChangeRequest(Long id, String adminEmail,
                                                                ReviewScheduleChangeRequest reviewRequest) {
        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule change request not found with id: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request has already been reviewed");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        String status = reviewRequest.getStatus().toUpperCase();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("Status must be APPROVED or REJECTED");
        }

        request.setStatus(status);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNote(reviewRequest.getReviewNote());

        if ("APPROVED".equals(status)) {
            applyScheduleChangeRequest(request);
        }

        request = scheduleChangeRequestRepository.save(request);
        return scheduleChangeRequestMapper.toDto(request);
    }

    private void applyScheduleChangeRequest(ScheduleChangeRequest request) {
        String requestType = request.getRequestType();

        if ("CANCEL".equals(requestType)) {
            if (request.getTrainingClass() != null) {
                trainingClassRepository.deleteById(request.getTrainingClass().getId());
            }
        } else if ("MODIFY".equals(requestType)) {
            if (request.getTrainingClass() != null) {
                TrainingClass trainingClass = request.getTrainingClass();
                if (request.getClassName() != null) {
                    trainingClass.setName(request.getClassName());
                }
                if (request.getClassDescription() != null) {
                    trainingClass.setDescription(request.getClassDescription());
                }
                if (request.getRequestedStartTime() != null) {
                    trainingClass.setStartTime(request.getRequestedStartTime());
                }
                if (request.getRequestedEndTime() != null) {
                    trainingClass.setEndTime(request.getRequestedEndTime());
                }
                if (request.getRequestedCapacity() != null) {
                    trainingClass.setCapacity(request.getRequestedCapacity());
                }
                if (request.getRequestedRoom() != null) {
                    trainingClass.setRoom(request.getRequestedRoom());
                }
                trainingClassRepository.save(trainingClass);
            }
        } else if ("ADD".equals(requestType)) {
            TrainingClass newClass = TrainingClass.builder()
                    .name(request.getClassName())
                    .description(request.getClassDescription())
                    .trainer(request.getTrainer())
                    .room(request.getRequestedRoom())
                    .startTime(request.getRequestedStartTime())
                    .endTime(request.getRequestedEndTime())
                    .capacity(request.getRequestedCapacity())
                    .reservations(new HashSet<>())
                    .build();
            trainingClassRepository.save(newClass);
        }
    }
}
