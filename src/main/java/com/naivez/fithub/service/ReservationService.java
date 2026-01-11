package com.naivez.fithub.service;

import com.naivez.fithub.dto.RatingRequest;
import com.naivez.fithub.dto.ReservationDTO;
import com.naivez.fithub.dto.ReservationRequest;
import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.entity.Reservation;
import com.naivez.fithub.entity.TrainingClass;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.exception.*;
import com.naivez.fithub.mapper.ReservationMapper;
import com.naivez.fithub.mapper.TrainingClassMapper;
import com.naivez.fithub.repository.ReservationRepository;
import com.naivez.fithub.repository.TrainingClassRepository;
import com.naivez.fithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrainingClassRepository trainingClassRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final TrainingClassMapper trainingClassMapper;
    private final MembershipService membershipService;
    private final NotificationService notificationService;


    @Transactional
    public ReservationDTO createReservation(String userEmail, ReservationRequest request) {
        log.info("Creating reservation - user: {}, classId: {}", userEmail, request.getTrainingClassId());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        if (!membershipService.hasActiveMembership(userEmail)) {
            log.warn("Reservation failed - no active membership for user: {}", userEmail);
            throw new NoActiveMembershipException("You must have an active membership to reserve a class");
        }

        TrainingClass trainingClass = trainingClassRepository.findById(request.getTrainingClassId())
                .orElseThrow(() -> new EntityNotFoundException("Training class not found"));

        if (trainingClass.getStartTime().isBefore(LocalDateTime.now())) {
            log.warn("Reservation failed - class already started: {}", trainingClass.getStartTime());
            throw new SessionAlreadyStartedException("Cannot reserve a class that has already started");
        }

        if (reservationRepository.existsByUserAndTrainingClassAndStatus(user, trainingClass, "CONFIRMED")) {
            log.warn("Reservation failed - duplicate reservation for user: {}, class: {}", userEmail, trainingClass.getId());
            throw new ReservationAlreadyExistsException("You already have a reservation for this class");
        }

        long confirmedCount = reservationRepository.countConfirmedReservationsByClassId(trainingClass.getId());
        if (confirmedCount >= trainingClass.getCapacity()) {
            log.warn("Reservation failed - class fully booked: {}, capacity: {}", trainingClass.getId(), trainingClass.getCapacity());
            throw new ClassFullyBookedException("Class is fully booked");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .trainingClass(trainingClass)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        reservation = reservationRepository.save(reservation);

        notificationService.createNotification(
                trainingClass.getTrainer(),
                "New reservation for your class on " + trainingClass.getStartTime()
        );

        log.info("Reservation created successfully - id: {}, user: {}, class: {}",
                reservation.getId(), userEmail, trainingClass.getId());

        return reservationMapper.toDto(reservation);
    }


    public List<TrainingClassDTO> getAvailableClasses() {
        LocalDateTime now = LocalDateTime.now();
        List<TrainingClass> classes = trainingClassRepository.findUpcomingClasses(now);

        return classes.stream()
                .map(trainingClassMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void cancelReservation(String userEmail, Long reservationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            log.warn("Reservation cancellation failed - user mismatch: {} vs {}", userEmail, reservation.getUser().getEmail());
            throw new UnauthorizedActionException("You can only cancel your own reservations");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            log.warn("Reservation cancellation failed - already cancelled: {}", reservationId);
            throw new ReservationAlreadyCancelledException("Reservation is already cancelled");
        }

        LocalDateTime classStartTime = reservation.getTrainingClass().getStartTime();
        if (classStartTime.isBefore(LocalDateTime.now())) {
            log.warn("Reservation cancellation failed - class already started: {}", classStartTime);
            throw new SessionAlreadyStartedException("Cannot cancel a reservation for a class that has already started");
        }

        Duration duration = Duration.between(LocalDateTime.now(), classStartTime);
        if (duration.toHours() < 2) {
            log.warn("Reservation cancellation failed - less than 2 hours before class: {} hours", duration.toHours());
            throw new ReservationCancellationTooLateException("Reservations can only be cancelled at least 2 hours before the class starts");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        notificationService.createNotification(
                reservation.getTrainingClass().getTrainer(),
                "Reservation was cancelled for your class on " +
                        reservation.getTrainingClass().getStartTime()
        );

        log.info("Reservation cancelled successfully - id: {}, user: {}", reservationId, userEmail);
    }

    @Transactional
    public void rateClass(String userEmail, Long reservationId, RatingRequest request) {
        log.info("Rating class - user: {}, reservationId: {}, rating: {}", userEmail, reservationId, request.getRating());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            log.warn("Rating failed - user mismatch: {} vs {}", userEmail, reservation.getUser().getEmail());
            throw new UnauthorizedActionException("You can only rate your own reservations");
        }

        LocalDateTime classEndTime = reservation.getTrainingClass().getEndTime();
        if (classEndTime.isAfter(LocalDateTime.now())) {
            log.warn("Rating failed - class not finished yet: {}", classEndTime);
            throw new InvalidRatingException("You can only rate classes that have already finished");
        }

        if (!"CONFIRMED".equals(reservation.getStatus())) {
            log.warn("Rating failed - reservation not confirmed: {}", reservation.getStatus());
            throw new InvalidRatingException("You can only rate classes you attended");
        }

        reservation.setRating(request.getRating());
        reservation.setComment(request.getComment());
        reservationRepository.save(reservation);

        updateClassAverageRating(reservation.getTrainingClass().getId());
        log.info("Class rated successfully - reservationId: {}, rating: {}", reservationId, request.getRating());
    }

    public List<ReservationDTO> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

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
        log.debug("Updating average rating for class: {}", classId);

        TrainingClass trainingClass = trainingClassRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Training class not found"));

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
            log.debug("Average rating updated for class: {} - new rating: {}", classId, average);
        }
    }
}
