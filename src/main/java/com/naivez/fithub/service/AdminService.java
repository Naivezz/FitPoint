package com.naivez.fithub.service;

import com.naivez.fithub.dto.*;
import com.naivez.fithub.entity.*;
import com.naivez.fithub.mapper.*;
import com.naivez.fithub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final TrainingClassRepository trainingClassRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ScheduleChangeRequestRepository scheduleChangeRequestRepository;

    private final RoomMapper roomMapper;
    private final EquipmentMapper equipmentMapper;
    private final PromotionMapper promotionMapper;
    private final CouponMapper couponMapper;
    private final TrainingClassMapper trainingClassMapper;
    private final UserMapper userMapper;
    private final ScheduleChangeRequestMapper scheduleChangeRequestMapper;
    private final ClassTypeMapper classTypeMapper;

    private final PasswordEncoder passwordEncoder;

    public List<ClassTypeDTO> getAllClassTypes() {
        List<TrainingClass> classes = trainingClassRepository.findAll();
        return classes.stream()
                .map(classTypeMapper::toClassTypeDTO)
                .collect(Collectors.groupingBy(ClassTypeDTO::getName))
                .values().stream()
                .map(list -> list.get(0))
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return roomMapper.toDto(room);
    }

    @Transactional
    public RoomDTO createRoom(RoomRequest request) {
        Room room = roomMapper.toEntity(request);
        if (room.getEquipmentList() == null) room.setEquipmentList(new HashSet<>());
        if (room.getClasses() == null) room.setClasses(new HashSet<>());
        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        roomMapper.updateFromRequest(request, room);

        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    public List<EquipmentDTO> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getEquipmentByRoom(Long roomId) {
        return equipmentRepository.findByRoomId(roomId).stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getEquipmentByStatus(String status) {
        return equipmentRepository.findByStatus(status).stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public EquipmentDTO getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
        return equipmentMapper.toDto(equipment);
    }

    @Transactional
    public EquipmentDTO createEquipment(EquipmentRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        Equipment equipment = equipmentMapper.toEntity(request);
        equipment.setRoom(room);

        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Transactional
    public EquipmentDTO updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        equipmentMapper.updateFromRequest(request, equipment);
        equipment.setRoom(room);

        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }

    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now()).stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }

    public PromotionDTO getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public PromotionDTO createPromotion(PromotionRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        Promotion promotion = promotionMapper.toEntity(request);
        promotion = promotionRepository.save(promotion);
        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        promotionMapper.updateFromRequest(request, promotion);
        promotion = promotionRepository.save(promotion);
        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found with id: " + id);
        }
        promotionRepository.deleteById(id);
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toDto)
                .collect(Collectors.toList());
    }

    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Coupon with code " + request.getCode() + " already exists");
        }

        Coupon coupon = couponMapper.toEntity(request);
        coupon = couponRepository.save(coupon);
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        if (!coupon.getCode().equals(request.getCode()) &&
                couponRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Coupon with code " + request.getCode() + " already exists");
        }

        couponMapper.updateFromRequest(request, coupon);
        coupon = couponRepository.save(coupon);
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Transactional
    public CouponDTO deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        coupon.setActive(false);
        coupon = couponRepository.save(coupon);
        return couponMapper.toDto(coupon);
    }

    public List<EmployeeDTO> getAllEmployees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "ROLE_TRAINER".equals(role.getName()) ||
                                "ROLE_ADMIN".equals(role.getName())))
                .map(userMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return userMapper.toEmployeeDTO(user);
    }

    @Transactional
    public EmployeeDTO createEmployee(CreateEmployeeRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
            Role role = roleRepository.findByName(fullRoleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + fullRoleName));
            roles.add(role);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .memberships(new HashSet<>())
                .reservations(new HashSet<>())
                .notifications(new HashSet<>())
                .build();

        user = userRepository.save(user);
        return userMapper.toEmployeeDTO(user);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        boolean isEmployee = user.getRoles().stream()
                .anyMatch(role -> "ROLE_TRAINER".equals(role.getName()) ||
                        "ROLE_ADMIN".equals(role.getName()));

        if (!isEmployee) {
            throw new RuntimeException("User is not an employee");
        }

        userRepository.deleteById(id);
    }

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

    @Transactional
    public TrainingClassDTO createTrainingClass(TrainingClassRequest request) {
        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found with id: " + request.getTrainerId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        TrainingClass trainingClass = trainingClassMapper.toEntity(request);
        trainingClass.setTrainer(trainer);
        trainingClass.setRoom(room);
        if (trainingClass.getReservations() == null) trainingClass.setReservations(new HashSet<>());

        trainingClass = trainingClassRepository.save(trainingClass);
        return trainingClassMapper.toDto(trainingClass);
    }

    @Transactional
    public TrainingClassDTO updateTrainingClass(Long id, TrainingClassRequest request) {
        TrainingClass trainingClass = trainingClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training class not found with id: " + id));

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found with id: " + request.getTrainerId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        trainingClassMapper.updateFromRequest(request, trainingClass);
        trainingClass.setTrainer(trainer);
        trainingClass.setRoom(room);

        trainingClass = trainingClassRepository.save(trainingClass);
        return trainingClassMapper.toDto(trainingClass);
    }

    @Transactional
    public void deleteTrainingClass(Long id) {
        if (!trainingClassRepository.existsById(id)) {
            throw new RuntimeException("Training class not found with id: " + id);
        }
        trainingClassRepository.deleteById(id);
    }

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

    public List<ClientProfileDTO> getAllClients() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "ROLE_CLIENT".equals(role.getName())))
                .map(userMapper::toClientProfileDTO)
                .collect(Collectors.toList());
    }

    public ClientProfileDTO getClientById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        return userMapper.toClientProfileDTO(user);
    }

    public List<MembershipTypeDTO> getMembershipTypes() {
        return List.of(
                MembershipTypeDTO.builder()
                        .type("MONTHLY")
                        .durationDays(30)
                        .price(new java.math.BigDecimal("99.99"))
                        .build(),
                MembershipTypeDTO.builder()
                        .type("QUARTERLY")
                        .durationDays(90)
                        .price(new java.math.BigDecimal("249.99"))
                        .build(),
                MembershipTypeDTO.builder()
                        .type("ANNUAL")
                        .durationDays(365)
                        .price(new java.math.BigDecimal("899.99"))
                        .build()
        );
    }
}