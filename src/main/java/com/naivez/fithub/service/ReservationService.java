package com.naivez.fithub.service;

import com.naivez.fithub.dto.RatingRequest;
import com.naivez.fithub.dto.ReservationDTO;
import com.naivez.fithub.dto.ReservationRequest;
import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.entity.Reservation;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.ReservationMapper;
import com.naivez.fithub.mapper.TrainingClassMapper;
import com.naivez.fithub.repository.ReservationRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrainingClassRepository trainingClassRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final TrainingClassMapper trainingClassMapper;

    public List<TrainingClassDTO> getAvailableClasses() {
        LocalDateTime now = LocalDateTime.now();
        List<TrainingClass> classes = trainingClassRepository.findUpcomingClasses(now);

        return classes.stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationDTO createReservation(String userEmail, ReservationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TrainingClass trainingClass = trainingClassRepository.findById(request.getTrainingClassId())
                .orElseThrow(() -> new RuntimeException("Training class not found"));

        if (trainingClass.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reserve a class that has already started");
        }

        if (reservationRepository.existsByUserAndTrainingClassAndStatus(user, trainingClass, "CONFIRMED")) {
            throw new RuntimeException("You already have a reservation for this class");
        }

        long confirmedCount = reservationRepository.countConfirmedReservationsByClassId(trainingClass.getId());
        if (confirmedCount >= trainingClass.getCapacity()) {
            throw new RuntimeException("Class is fully booked");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .trainingClass(trainingClass)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        reservation = reservationRepository.save(reservation);

        return reservationMapper.toDto(reservation);
    }

    @Transactional
    public void cancelReservation(String userEmail, Long reservationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only cancel your own reservations");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new RuntimeException("Reservation is already cancelled");
        }

        LocalDateTime classStartTime = reservation.getTrainingClass().getStartTime();
        if (classStartTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel a reservation for a class that has already started");
        }

        Duration duration = Duration.between(LocalDateTime.now(), classStartTime);
        if (duration.toHours() < 2) {
            throw new RuntimeException("Reservations can only be cancelled at least 2 hours before the class starts");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    @Transactional
    public void rateClass(String userEmail, Long reservationId, RatingRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only rate your own reservations");
        }

        LocalDateTime classEndTime = reservation.getTrainingClass().getEndTime();
        if (classEndTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("You can only rate classes that have already finished");
        }

        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new RuntimeException("You can only rate classes you attended");
        }

        reservation.setRating(request.getRating());
        reservation.setComment(request.getComment());
        reservationRepository.save(reservation);

        updateClassAverageRating(reservation.getTrainingClass().getId());
    }

    public List<ReservationDTO> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Reservation> reservations = reservationRepository.findByUserWithTrainingClass(user.getId());

        return reservations.stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getUpcomingReservations(String userEmail) {
        List<ReservationDTO> allReservations = getMyReservations(userEmail);
        LocalDateTime now = LocalDateTime.now();

        return allReservations.stream()
                .filter(r -> r.getClassStartTime().isAfter(now) && "CONFIRMED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getPastReservations(String userEmail) {
        List<ReservationDTO> allReservations = getMyReservations(userEmail);
        LocalDateTime now = LocalDateTime.now();

        return allReservations.stream()
                .filter(r -> r.getClassEndTime().isBefore(now))
                .collect(Collectors.toList());
    }

    private void updateClassAverageRating(Long classId) {
        TrainingClass trainingClass = trainingClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Training class not found"));

        List<Reservation> ratedReservations = trainingClass.getReservations().stream()
                .filter(r -> r.getRating() != null)
                .collect(Collectors.toList());

        if (!ratedReservations.isEmpty()) {
            double average = ratedReservations.stream()
                    .mapToInt(Reservation::getRating)
                    .average()
                    .orElse(0.0);
            trainingClass.setAverageRating(average);
            trainingClassRepository.save(trainingClass);
        }
    }
}
