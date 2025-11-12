package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.*;
import com.naivez.fithub.exception.ClassFullyBookedException;
import com.naivez.fithub.exception.IncorrectPasswordException;
import com.naivez.fithub.exception.InvalidRatingException;
import com.naivez.fithub.exception.InvalidRequestDataException;
import com.naivez.fithub.service.ClientService;
import com.naivez.fithub.service.MembershipService;
import com.naivez.fithub.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private ClientService clientService;

    private TrainingClassDTO testClassDTO;
    private ReservationDTO testReservationDTO;
    private MembershipDTO testMembershipDTO;
    private UserProfileDTO testUserProfileDTO;
    private ReservationRequest reservationRequest;
    private PurchaseMembershipRequest purchaseMembershipRequest;

    @BeforeEach
    void setUp() {
        testClassDTO = TrainingClassDTO.builder()
                .id(1L)
                .name("trainingClass1")
                .description("description1")
                .trainerName("user1")
                .roomName("room1")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(15)
                .availableSpots(5)
                .build();

        testReservationDTO = ReservationDTO.builder()
                .id(1L)
                .trainingClassId(1L)
                .className("trainingClass1")
                .trainerName("user1")
                .classStartTime(LocalDateTime.now().plusDays(1))
                .classEndTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        testMembershipDTO = MembershipDTO.builder()
                .id(1L)
                .type("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(new BigDecimal("99.99"))
                .active(true)
                .daysRemaining(30)
                .build();

        testUserProfileDTO = UserProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user2")
                .lastName("user1")
                .phone("1111111111")
                .build();

        reservationRequest = ReservationRequest.builder()
                .trainingClassId(1L)
                .build();

        purchaseMembershipRequest = PurchaseMembershipRequest.builder()
                .type("MONTHLY")
                .build();
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getAvailableClasses_shouldReturnClassList() throws Exception {
        when(reservationService.getAvailableClasses()).thenReturn(List.of(testClassDTO));

        mockMvc.perform(get("/api/client/classes/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("trainingClass1"))
                .andExpect(jsonPath("$[0].description").value("description1"))
                .andExpect(jsonPath("$[0].trainerName").value("user1"))
                .andExpect(jsonPath("$[0].roomName").value("room1"))
                .andExpect(jsonPath("$[0].capacity").value(15))
                .andExpect(jsonPath("$[0].availableSpots").value(5));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void createReservation_withValidRequest_shouldReturnCreatedReservation() throws Exception {
        when(reservationService.createReservation(eq("user1@gmail.com"), any(ReservationRequest.class)))
                .thenReturn(testReservationDTO);

        mockMvc.perform(post("/api/client/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.className").value("trainingClass1"))
                .andExpect(jsonPath("$.trainerName").value("user1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void createReservation_whenClassFull_shouldReturnBadRequest() throws Exception {
        when(reservationService.createReservation(eq("user1@gmail.com"), any(ReservationRequest.class)))
                .thenThrow(new ClassFullyBookedException("Class is fully booked"));

        mockMvc.perform(post("/api/client/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void cancelReservation_withValidId_shouldReturnNoContent() throws Exception {
        doNothing().when(reservationService).cancelReservation("user1@gmail.com", 1L);

        mockMvc.perform(delete("/api/client/reservations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void getMyReservations_shouldReturnReservationList() throws Exception {
        when(reservationService.getMyReservations("user1@gmail.com"))
                .thenReturn(List.of(testReservationDTO));

        mockMvc.perform(get("/api/client/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].className").value("trainingClass1"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void getActiveMemberships_shouldReturnMembershipList() throws Exception {
        when(membershipService.getActiveMemberships("user1@gmail.com"))
                .thenReturn(List.of(testMembershipDTO));

        mockMvc.perform(get("/api/client/memberships/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("MONTHLY"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].daysRemaining").value(30))
                .andExpect(jsonPath("$[0].price").value(99.99));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void purchaseMembership_withValidRequest_shouldReturnPurchasedMembership() throws Exception {
        when(membershipService.purchaseMembership(eq("user1@gmail.com"), any(PurchaseMembershipRequest.class)))
                .thenReturn(testMembershipDTO);

        mockMvc.perform(post("/api/client/memberships/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseMembershipRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("MONTHLY"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void purchaseMembership_whenInvalidType_shouldReturnBadRequest() throws Exception {
        PurchaseMembershipRequest invalidRequest = PurchaseMembershipRequest.builder()
                .type("INVALID_TYPE")
                .build();

        when(membershipService.purchaseMembership(eq("user1@gmail.com"), any(PurchaseMembershipRequest.class)))
                .thenThrow(new InvalidRequestDataException("Invalid membership type"));

        mockMvc.perform(post("/api/client/memberships/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void getProfile_shouldReturnUserProfile() throws Exception {
        when(clientService.getProfile("user1@gmail.com")).thenReturn(testUserProfileDTO);

        mockMvc.perform(get("/api/client/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user1@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("user2"))
                .andExpect(jsonPath("$.lastName").value("user1"))
                .andExpect(jsonPath("$.phone").value("1111111111"));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void updateProfile_withValidRequest_shouldReturnUpdatedProfile() throws Exception {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .firstName("name")
                .lastName("name1")
                .email("user1@gmail.com")
                .phone("2222222222")
                .build();

        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("name")
                .lastName("name1")
                .phone("2222222222")
                .build();

        when(clientService.updateProfile(eq("user1@gmail.com"), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/client/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("name"))
                .andExpect(jsonPath("$.lastName").value("name1"))
                .andExpect(jsonPath("$.phone").value("2222222222"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void clientEndpoints_withAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/client/profile"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/client/reservations"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/client/memberships/active"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void clientEndpoints_withTrainerRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/client/profile"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/client/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/client/memberships/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseMembershipRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void rateClass_withValidRequest_shouldReturnOk() throws Exception {
        RatingRequest ratingRequest = RatingRequest.builder()
                .rating(5)
                .comment("Great class!")
                .build();

        doNothing().when(reservationService).rateClass(eq("user1@gmail.com"), eq(1L), any(RatingRequest.class));

        mockMvc.perform(put("/api/client/reservations/1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void rateClass_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
        RatingRequest ratingRequest = RatingRequest.builder()
                .rating(5)
                .comment("Great class!")
                .build();

        doThrow(new InvalidRatingException("Cannot rate class")).when(reservationService)
                .rateClass(eq("user1@gmail.com"), eq(1L), any(RatingRequest.class));

        mockMvc.perform(put("/api/client/reservations/1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void topUpBalance_withValidRequest_shouldReturnOk() throws Exception {
        when(membershipService.topUpBalance(eq("user1@gmail.com"), any(PurchaseMembershipRequest.class)))
                .thenReturn(testMembershipDTO);

        mockMvc.perform(post("/api/client/memberships/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseMembershipRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("MONTHLY"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void topUpBalance_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
        when(membershipService.topUpBalance(eq("user1@gmail.com"), any(PurchaseMembershipRequest.class)))
                .thenThrow(new InvalidRequestDataException("Top up failed"));

        mockMvc.perform(post("/api/client/memberships/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseMembershipRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void changePassword_withValidRequest_shouldReturnOk() throws Exception {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        doNothing().when(clientService).changePassword(eq("user1@gmail.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/client/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void changePassword_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        doThrow(new IncorrectPasswordException("Password change failed")).when(clientService)
                .changePassword(eq("user1@gmail.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/client/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void getUpcomingReservations_shouldReturnUpcomingReservations() throws Exception {
        when(reservationService.getUpcomingReservations("user1@gmail.com"))
                .thenReturn(List.of(testReservationDTO));

        mockMvc.perform(get("/api/client/reservations/upcoming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].className").value("trainingClass1"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void getPastReservations_shouldReturnPastReservations() throws Exception {
        when(reservationService.getPastReservations("user1@gmail.com"))
                .thenReturn(List.of(testReservationDTO));

        mockMvc.perform(get("/api/client/reservations/past"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].className").value("trainingClass1"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "user1@gmail.com")
    void getUserMemberships_shouldReturnAllMemberships() throws Exception {
        MembershipDTO inactiveMembership = MembershipDTO.builder()
                .id(2L)
                .type("QUARTERLY")
                .startDate(LocalDate.now().minusDays(100))
                .endDate(LocalDate.now().minusDays(10))
                .price(new BigDecimal("249.99"))
                .active(false)
                .daysRemaining(0)
                .build();

        when(membershipService.getUserMemberships("user1@gmail.com"))
                .thenReturn(List.of(testMembershipDTO, inactiveMembership));

        mockMvc.perform(get("/api/client/memberships"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("MONTHLY"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].type").value("QUARTERLY"))
                .andExpect(jsonPath("$[1].active").value(false));
    }
}