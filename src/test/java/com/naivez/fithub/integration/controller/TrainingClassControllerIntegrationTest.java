package com.naivez.fithub.integration.controller;

import com.naivez.fithub.dto.TrainingClassDTO;
import com.naivez.fithub.dto.TrainingClassRequest;
import com.naivez.fithub.service.TrainingClassService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TrainingClassControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainingClassService trainingClassService;

    private static final Long TRAINING_CLASS_ID = 1L;
    private TrainingClassDTO testTrainingClassDTO;
    private TrainingClassRequest testTrainingClassRequest;

    @BeforeEach
    void setUp() {
        testTrainingClassDTO = TrainingClassDTO.builder()
                .id(TRAINING_CLASS_ID)
                .name("trainingClass1")
                .description("test training session 1")
                .trainerName("trainer1")
                .roomName("room1")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(20)
                .availableSpots(15)
                .averageRating(4.5)
                .build();

        testTrainingClassRequest = TrainingClassRequest.builder()
                .name("trainingClass1")
                .description("test training session 1")
                .trainerId(1L)
                .roomId(1L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(20)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTrainingClasses_AsAdmin_ShouldReturnClasses() throws Exception {
        List<TrainingClassDTO> classes = Arrays.asList(testTrainingClassDTO);

        when(trainingClassService.getAllTrainingClasses()).thenReturn(classes);

        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(TRAINING_CLASS_ID))
                .andExpect(jsonPath("$[0].name").value("trainingClass1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUpcomingClasses_AsAdmin_ShouldReturnClasses() throws Exception {
        List<TrainingClassDTO> classes = Arrays.asList(testTrainingClassDTO);

        when(trainingClassService.getUpcomingClasses()).thenReturn(classes);

        mockMvc.perform(get("/api/classes/upcoming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(TRAINING_CLASS_ID));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClassesBetween_AsAdmin_ShouldReturnClasses() throws Exception {
        List<TrainingClassDTO> classes = Arrays.asList(testTrainingClassDTO);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        when(trainingClassService.getClassesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(classes);

        mockMvc.perform(get("/api/classes/between")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(TRAINING_CLASS_ID));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTrainingClass_AsAdmin_ShouldCreateClass() throws Exception {
        when(trainingClassService.createTrainingClass(any(TrainingClassRequest.class)))
                .thenReturn(testTrainingClassDTO);

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTrainingClassRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TRAINING_CLASS_ID))
                .andExpect(jsonPath("$.name").value("trainingClass1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTrainingClass_AsAdmin_ShouldUpdateClass() throws Exception {
        when(trainingClassService.updateTrainingClass(eq(TRAINING_CLASS_ID), any(TrainingClassRequest.class)))
                .thenReturn(testTrainingClassDTO);

        mockMvc.perform(put("/api/classes/{id}", TRAINING_CLASS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTrainingClassRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TRAINING_CLASS_ID))
                .andExpect(jsonPath("$.name").value("trainingClass1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTrainingClass_AsAdmin_ShouldDeleteClass() throws Exception {
        mockMvc.perform(delete("/api/classes/{id}", TRAINING_CLASS_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void createTrainingClass_AsTrainer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTrainingClassRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTrainingClasses_Unauthenticated_ShouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isForbidden());
    }
}