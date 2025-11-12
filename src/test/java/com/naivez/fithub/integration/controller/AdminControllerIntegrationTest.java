package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.*;
import com.naivez.fithub.exception.EmailAlreadyExistsException;
import com.naivez.fithub.exception.EntityNotFoundException;
import com.naivez.fithub.exception.UserNotFoundException;
import com.naivez.fithub.service.EmployeeService;
import com.naivez.fithub.service.ScheduleChangeRequestService;
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

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ScheduleChangeRequestService scheduleChangeRequestService;

    private EmployeeDTO testEmployeeDTO;
    private CreateEmployeeRequest createEmployeeRequest;
    private ScheduleChangeRequestDTO testScheduleChangeRequestDTO;
    private ReviewScheduleChangeRequest reviewRequest;

    @BeforeEach
    void setUp() {
        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .build();

        createEmployeeRequest = CreateEmployeeRequest.builder()
                .email("newuser1@gmail.com")
                .password("password123")
                .firstName("user2")
                .lastName("user2")
                .phone("2222222222")
                .roles(Set.of("ROLE_TRAINER"))
                .build();

        testScheduleChangeRequestDTO = ScheduleChangeRequestDTO.builder()
                .id(1L)
                .requestType("ADD")
                .className("trainingClass5")
                .reason("reason")
                .status("PENDING")
                .build();

        reviewRequest = ReviewScheduleChangeRequest.builder()
                .status("APPROVED")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllEmployees_withAdminRole_shouldReturnEmployeeList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(testEmployeeDTO));

        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@gmail.com"))
                .andExpect(jsonPath("$[0].firstName").value("user1"))
                .andExpect(jsonPath("$[0].lastName").value("user1"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getAllEmployees_withClientRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void getAllEmployees_withTrainerRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployeeById_whenEmployeeExists_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        mockMvc.perform(get("/api/admin/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user1@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("user1"))
                .andExpect(jsonPath("$.lastName").value("user1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployeeById_whenEmployeeNotFound_shouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeById(999L)).thenThrow(new UserNotFoundException("Employee not found"));

        mockMvc.perform(get("/api/admin/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_withValidRequest_shouldReturnCreatedEmployee() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(testEmployeeDTO);

        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user1@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("user1"))
                .andExpect(jsonPath("$.lastName").value("user1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_whenEmailAlreadyExists_shouldReturnBadRequest() throws Exception {
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_whenEmployeeExists_shouldReturnNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/admin/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllScheduleChangeRequests_shouldReturnRequestList() throws Exception {
        when(scheduleChangeRequestService.getAllScheduleChangeRequests())
                .thenReturn(List.of(testScheduleChangeRequestDTO));

        mockMvc.perform(get("/api/admin/schedule-change-requests"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].requestType").value("ADD"))
                .andExpect(jsonPath("$[0].className").value("trainingClass5"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingScheduleChangeRequests_shouldReturnPendingRequests() throws Exception {
        when(scheduleChangeRequestService.getPendingScheduleChangeRequests())
                .thenReturn(List.of(testScheduleChangeRequestDTO));

        mockMvc.perform(get("/api/admin/schedule-change-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getScheduleChangeRequestById_whenRequestExists_shouldReturnRequest() throws Exception {
        when(scheduleChangeRequestService.getScheduleChangeRequestById(1L))
                .thenReturn(testScheduleChangeRequestDTO);

        mockMvc.perform(get("/api/admin/schedule-change-requests/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.requestType").value("ADD"))
                .andExpect(jsonPath("$.className").value("trainingClass5"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getScheduleChangeRequestById_whenRequestNotFound_shouldReturnNotFound() throws Exception {
        when(scheduleChangeRequestService.getScheduleChangeRequestById(999L))
                .thenThrow(new EntityNotFoundException("Request not found"));

        mockMvc.perform(get("/api/admin/schedule-change-requests/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin1@gmail.com")
    void reviewScheduleChangeRequest_withValidRequest_shouldReturnReviewedRequest() throws Exception {
        ScheduleChangeRequestDTO reviewedRequest = ScheduleChangeRequestDTO.builder()
                .id(1L)
                .requestType("ADD")
                .className("trainingClass5")
                .reason("reason")
                .status("APPROVED")
                .build();

        when(scheduleChangeRequestService.reviewScheduleChangeRequest(eq(1L), eq("admin1@gmail.com"), any(ReviewScheduleChangeRequest.class)))
                .thenReturn(reviewedRequest);

        mockMvc.perform(put("/api/admin/schedule-change-requests/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin1@gmail.com")
    void reviewScheduleChangeRequest_whenRequestNotFound_shouldReturnNotFound() throws Exception {
        when(scheduleChangeRequestService.reviewScheduleChangeRequest(eq(999L), eq("admin1@gmail.com"), any(ReviewScheduleChangeRequest.class)))
                .thenThrow(new EntityNotFoundException("Request not found"));

        mockMvc.perform(put("/api/admin/schedule-change-requests/999/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void scheduleChangeRequestEndpoints_withClientRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/schedule-change-requests"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/schedule-change-requests/pending"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/schedule-change-requests/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/schedule-change-requests/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void employeeEndpoints_withTrainerRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployeeRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/admin/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        CreateEmployeeRequest invalidRequest = CreateEmployeeRequest.builder()
                .email("")
                .password("")
                .firstName("")
                .lastName("")
                .build();

        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reviewScheduleChangeRequest_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        ReviewScheduleChangeRequest invalidReview = ReviewScheduleChangeRequest.builder()
                .status("")
                .build();

        mockMvc.perform(put("/api/admin/schedule-change-requests/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
    }
}