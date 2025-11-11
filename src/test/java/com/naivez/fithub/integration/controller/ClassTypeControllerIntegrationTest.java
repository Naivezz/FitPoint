package com.naivez.fithub.integration.controller;

import com.naivez.fithub.dto.ClassTypeDTO;
import com.naivez.fithub.service.ClassTypeService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClassTypeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClassTypeService classTypeService;

    private List<ClassTypeDTO> testClassTypes;

    @BeforeEach
    void setUp() {
        testClassTypes = List.of(
                ClassTypeDTO.builder()
                        .name("classType1")
                        .description("test class description 1")
                        .build(),
                ClassTypeDTO.builder()
                        .name("classType2")
                        .description("test class description 2")
                        .build(),
                ClassTypeDTO.builder()
                        .name("classType5")
                        .description("test class description 3")
                        .build()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClassTypes_withAdminRole_shouldReturnClassTypeList() throws Exception {
        when(classTypeService.getAllClassTypes()).thenReturn(testClassTypes);

        mockMvc.perform(get("/api/class-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("classType1"))
                .andExpect(jsonPath("$[0].description").value("test class description 1"))
                .andExpect(jsonPath("$[1].name").value("classType2"))
                .andExpect(jsonPath("$[1].description").value("test class description 2"))
                .andExpect(jsonPath("$[2].name").value("classType5"))
                .andExpect(jsonPath("$[2].description").value("test class description 3"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClassTypes_whenNoClassTypes_shouldReturnEmptyList() throws Exception {
        when(classTypeService.getAllClassTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/class-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}