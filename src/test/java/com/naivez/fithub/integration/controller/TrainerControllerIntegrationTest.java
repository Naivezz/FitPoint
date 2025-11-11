package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.*;
import com.naivez.fithub.service.NotificationService;
import com.naivez.fithub.service.PersonalTrainingSessionService;
import com.naivez.fithub.service.TrainerService;
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
class TrainerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerService trainerService;

    @MockBean
    private PersonalTrainingSessionService personalTrainingSessionService;

    @MockBean
    private NotificationService notificationService;

    private TrainingClassDTO testTrainingClassDTO;
    private PersonalTrainingSessionDTO testPersonalSessionDTO;
    private ScheduleChangeRequestDTO testScheduleChangeRequestDTO;
    private ClientProfileDTO testClientProfileDTO;
    private TrainerNoteDTO testTrainerNoteDTO;
    private UserProfileDTO testUserProfileDTO;
    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        testTrainingClassDTO = TrainingClassDTO.builder()
                .id(1L)
                .name("trainingClass1")
                .description("description1")
                .trainerName("trainer1")
                .roomName("room1")
                .startTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .capacity(15)
                .availableSpots(10)
                .averageRating(4.5)
                .build();

        testPersonalSessionDTO = PersonalTrainingSessionDTO.builder()
                .id(1L)
                .clientId(1L)
                .clientName("user1")
                .startTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0))
                .sessionGoal("goal1")
                .sessionNotes("notes1")
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testScheduleChangeRequestDTO = ScheduleChangeRequestDTO.builder()
                .id(1L)
                .trainingClassId(1L)
                .requestType("RESCHEDULE")
                .reason("reason1")
                .className("trainingClass1")
                .classDescription("description1")
                .requestedStartTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0))
                .requestedEndTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0))
                .requestedCapacity(15)
                .requestedRoomId(1L)
                .requestedRoomName("room1")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .reviewedAt(null)
                .reviewedByName(null)
                .reviewNote(null)
                .build();

        testClientProfileDTO = ClientProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user2")
                .lastName("user1")
                .phone("1111111111")
                .fitnessGoals("goal1")
                .medicalNotes("No injuries")
                .build();

        testTrainerNoteDTO = TrainerNoteDTO.builder()
                .id(1L)
                .clientId(1L)
                .clientName("user1")
                .note("test trainer note 8")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserProfileDTO = UserProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user2")
                .lastName("user1")
                .phone("1111111111")
                .build();

        testNotificationDTO = NotificationDTO.builder()
                .id(1L)
                .message("message")
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getDailySchedule_shouldReturnDailyClasses() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        when(trainerService.getDailySchedule("trainer1@gmail.com", date))
                .thenReturn(List.of(testTrainingClassDTO));

        mockMvc.perform(get("/api/trainer/schedule/daily")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("trainingClass1"))
                .andExpect(jsonPath("$[0].trainerName").value("trainer1"))
                .andExpect(jsonPath("$[0].averageRating").value(4.5));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getWeeklySchedule_shouldReturnWeeklyClasses() throws Exception {
        LocalDate startDate = LocalDate.now().plusDays(1);
        when(trainerService.getWeeklySchedule("trainer1@gmail.com", startDate))
                .thenReturn(List.of(testTrainingClassDTO));

        mockMvc.perform(get("/api/trainer/schedule/weekly")
                        .param("startDate", startDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("trainingClass1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getPersonalSessionsInRange_shouldReturnSessions() throws Exception {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(personalTrainingSessionService.getPersonalSessionsInRange("trainer1@gmail.com", startDate, endDate))
                .thenReturn(List.of(testPersonalSessionDTO));

        mockMvc.perform(get("/api/trainer/schedule/personal-sessions")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].clientName").value("user1"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].sessionGoal").value("goal1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void createScheduleChangeRequest_withValidRequest_shouldReturnCreated() throws Exception {
        ScheduleChangeRequestRequest request = ScheduleChangeRequestRequest.builder()
                .trainingClassId(1L)
                .requestType("RESCHEDULE")
                .reason("reason1")
                .requestedStartTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0))
                .requestedEndTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0))
                .build();

        when(trainerService.createScheduleChangeRequest(eq("trainer1@gmail.com"), any(ScheduleChangeRequestRequest.class)))
                .thenReturn(testScheduleChangeRequestDTO);

        mockMvc.perform(post("/api/trainer/schedule/change-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requestType").value("RESCHEDULE"))
                .andExpect(jsonPath("$.reason").value("reason1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getScheduleChangeRequests_shouldReturnAllRequests() throws Exception {
        when(trainerService.getScheduleChangeRequests("trainer1@gmail.com"))
                .thenReturn(List.of(testScheduleChangeRequestDTO));

        mockMvc.perform(get("/api/trainer/schedule/change-requests"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getPendingScheduleChangeRequests_shouldReturnPendingRequests() throws Exception {
        when(trainerService.getPendingScheduleChangeRequests("trainer1@gmail.com"))
                .thenReturn(List.of(testScheduleChangeRequestDTO));

        mockMvc.perform(get("/api/trainer/schedule/change-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getClients_shouldReturnClientList() throws Exception {
        when(trainerService.getTrainerClients("trainer1@gmail.com"))
                .thenReturn(List.of(testClientProfileDTO));

        mockMvc.perform(get("/api/trainer/clients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("user2"))
                .andExpect(jsonPath("$[0].lastName").value("user1"))
                .andExpect(jsonPath("$[0].fitnessGoals").value("goal1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getClientProfile_withValidId_shouldReturnClient() throws Exception {
        when(trainerService.getClientProfile("trainer1@gmail.com", 1L))
                .thenReturn(testClientProfileDTO);

        mockMvc.perform(get("/api/trainer/clients/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user1@gmail.com"))
                .andExpect(jsonPath("$.fitnessGoals").value("goal1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getClientProfile_whenClientNotFound_shouldReturnBadRequest() throws Exception {
        when(trainerService.getClientProfile("trainer1@gmail.com", 999L))
                .thenThrow(new RuntimeException("Client not found"));

        mockMvc.perform(get("/api/trainer/clients/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void addClientNote_withValidRequest_shouldReturnCreated() throws Exception {
        TrainerNoteRequest noteRequest = TrainerNoteRequest.builder()
                .clientId(1L)
                .note("test trainer note 7")
                .build();

        when(trainerService.addClientNote(eq("trainer1@gmail.com"), any(TrainerNoteRequest.class)))
                .thenReturn(testTrainerNoteDTO);

        mockMvc.perform(post("/api/trainer/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.note").value("test trainer note 8"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void updateClientNote_withValidRequest_shouldReturnUpdatedNote() throws Exception {
        TrainerNoteRequest updateRequest = TrainerNoteRequest.builder()
                .clientId(1L)
                .note("note")
                .build();

        TrainerNoteDTO updatedNote = TrainerNoteDTO.builder()
                .id(1L)
                .clientId(1L)
                .clientName("user1")
                .note("note")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(trainerService.updateClientNote(eq("trainer1@gmail.com"), eq(1L), any(TrainerNoteRequest.class)))
                .thenReturn(updatedNote);

        mockMvc.perform(put("/api/trainer/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.note").value("note"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void deleteClientNote_withValidId_shouldReturnNoContent() throws Exception {
        doNothing().when(trainerService).deleteClientNote("trainer1@gmail.com", 1L);

        mockMvc.perform(delete("/api/trainer/notes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getAllNotes_shouldReturnAllNotes() throws Exception {
        when(trainerService.getAllNotes("trainer1@gmail.com"))
                .thenReturn(List.of(testTrainerNoteDTO));

        mockMvc.perform(get("/api/trainer/notes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].clientName").value("user1"))
                .andExpect(jsonPath("$[0].note").value("test trainer note 8"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getClientNotes_shouldReturnClientNotes() throws Exception {
        when(trainerService.getClientNotes("trainer1@gmail.com", 1L))
                .thenReturn(List.of(testTrainerNoteDTO));

        mockMvc.perform(get("/api/trainer/notes/client/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].clientId").value(1L))
                .andExpect(jsonPath("$[0].note").value("test trainer note 8"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void createPersonalSession_withValidRequest_shouldReturnCreated() throws Exception {
        PersonalTrainingSessionRequest sessionRequest = PersonalTrainingSessionRequest.builder()
                .clientId(1L)
                .startTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0))
                .sessionGoal("goal")
                .sessionNotes("session goal")
                .build();

        when(personalTrainingSessionService.createPersonalSession(eq("trainer1@gmail.com"), any(PersonalTrainingSessionRequest.class)))
                .thenReturn(testPersonalSessionDTO);

        mockMvc.perform(post("/api/trainer/personal-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.sessionGoal").value("goal1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void updatePersonalSession_withValidRequest_shouldReturnUpdatedSession() throws Exception {
        PersonalTrainingSessionRequest updateRequest = PersonalTrainingSessionRequest.builder()
                .clientId(1L)
                .startTime(LocalDateTime.now().plusDays(3).withHour(15).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0))
                .sessionGoal("goals2")
                .sessionNotes("Rescheduled session")
                .build();

        PersonalTrainingSessionDTO updatedSession = PersonalTrainingSessionDTO.builder()
                .id(1L)
                .clientId(1L)
                .clientName("user1")
                .startTime(LocalDateTime.now().plusDays(3).withHour(15).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0))
                .sessionGoal("goals")
                .sessionNotes("session")
                .status("RESCHEDULED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(personalTrainingSessionService.updatePersonalSession(eq("trainer1@gmail.com"), eq(1L), any(PersonalTrainingSessionRequest.class)))
                .thenReturn(updatedSession);

        mockMvc.perform(put("/api/trainer/personal-sessions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("RESCHEDULED"))
                .andExpect(jsonPath("$.sessionGoal").value("goals"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void cancelPersonalSession_withValidId_shouldReturnNoContent() throws Exception {
        doNothing().when(personalTrainingSessionService).cancelPersonalSession("trainer1@gmail.com", 1L);

        mockMvc.perform(delete("/api/trainer/personal-sessions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void completePersonalSession_withValidId_shouldReturnCompletedSession() throws Exception {
        PersonalTrainingSessionDTO completedSession = PersonalTrainingSessionDTO.builder()
                .id(1L)
                .clientId(1L)
                .clientName("user1")
                .startTime(LocalDateTime.now().withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().withHour(15).withMinute(0))
                .sessionGoal("goal")
                .sessionNotes("note")
                .status("COMPLETED")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        when(personalTrainingSessionService.completePersonalSession(eq("trainer1@gmail.com"), eq(1L), anyString()))
                .thenReturn(completedSession);

        mockMvc.perform(put("/api/trainer/personal-sessions/1/complete")
                        .param("sessionNotes", "note"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sessionNotes").value("note"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getAllPersonalSessions_shouldReturnAllSessions() throws Exception {
        when(personalTrainingSessionService.getAllPersonalSessions("trainer1@gmail.com"))
                .thenReturn(List.of(testPersonalSessionDTO));

        mockMvc.perform(get("/api/trainer/personal-sessions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].clientName").value("user1"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getClientPersonalSessions_shouldReturnClientSessions() throws Exception {
        when(personalTrainingSessionService.getClientPersonalSessions("trainer1@gmail.com", 1L))
                .thenReturn(List.of(testPersonalSessionDTO));

        mockMvc.perform(get("/api/trainer/personal-sessions/client/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].clientId").value(1L))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getClassRegistrations_shouldReturnRegistrations() throws Exception {
        when(trainerService.getClassRegistrations("trainer1@gmail.com", 1L))
                .thenReturn(List.of(testUserProfileDTO));

        mockMvc.perform(get("/api/trainer/classes/1/registrations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("user2"))
                .andExpect(jsonPath("$[0].lastName").value("user1"));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getNotifications_shouldReturnAllNotifications() throws Exception {
        when(notificationService.getUserNotifications("trainer1@gmail.com"))
                .thenReturn(List.of(testNotificationDTO));

        mockMvc.perform(get("/api/trainer/notifications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].message").value("message"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void getUnreadNotifications_shouldReturnUnreadNotifications() throws Exception {
        when(notificationService.getUnreadNotifications("trainer1@gmail.com"))
                .thenReturn(List.of(testNotificationDTO));

        mockMvc.perform(get("/api/trainer/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void markNotificationAsRead_shouldReturnOk() throws Exception {
        doNothing().when(notificationService).markAsRead("trainer1@gmail.com", 1L);

        mockMvc.perform(put("/api/trainer/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer1@gmail.com")
    void markAllNotificationsAsRead_shouldReturnOk() throws Exception {
        doNothing().when(notificationService).markAllAsRead("trainer1@gmail.com");

        mockMvc.perform(put("/api/trainer/notifications/read-all"))
                .andExpect(status().isOk());
    }
}