package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanService;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.PlanResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.PlanPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Catalogo de planos disponiveis para contas proprietarias.")
public class PlanController {
    private final PlanService service;

    @GetMapping
    @Operation(summary = "Lista os planos ativos")
    public ResponseEntity<List<PlanResponseDTO>> findAllActive() {
        List<Plan> plans = service.findAllActive();

        return ResponseEntity.ok(PlanPresentationMapper.toResponseDTO(plans));
    }
}
