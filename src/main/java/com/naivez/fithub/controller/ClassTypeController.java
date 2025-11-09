package com.naivez.fithub.controller;

import com.naivez.fithub.dto.ClassTypeDTO;
import com.naivez.fithub.service.ClassTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-types")
@RequiredArgsConstructor
public class ClassTypeController {

    private final ClassTypeService classTypeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'CLIENT')")
    public ResponseEntity<List<ClassTypeDTO>> getAllClassTypes() {
        List<ClassTypeDTO> classTypes = classTypeService.getAllClassTypes();
        return ResponseEntity.ok(classTypes);
    }
}
