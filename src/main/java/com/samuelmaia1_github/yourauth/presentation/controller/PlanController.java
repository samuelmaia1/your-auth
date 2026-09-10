package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.plan.Plan;
import com.samuelmaia1_github.yourauth.domain.plan.PlanCode;
import com.samuelmaia1_github.yourauth.domain.plan.PlanLimitSettings;
import com.samuelmaia1_github.yourauth.domain.plan.PlanService;
import com.samuelmaia1_github.yourauth.presentation.dto.error.ErrorResponse;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.PlanResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.plan.UpdatePlanLimitsDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.PlanPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PutMapping("/{code}/limits")
    @Operation(
            summary = "Atualiza os limites de uso de um plano",
            description = "Atualiza apenas os limites enviados no corpo da requisicao. Campos omitidos permanecem com o valor atual; campos enviados como null removem o limite numerico."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Limites do plano atualizados.",
                    content = @Content(schema = @Schema(implementation = PlanResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisicao invalido.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plano nao encontrado.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PlanResponseDTO> updateLimits(
            @PathVariable PlanCode code,
            @Valid @RequestBody UpdatePlanLimitsDTO dto
    ) {
        PlanLimitSettings settings = PlanPresentationMapper.toLimitSettings(dto);
        Plan plan = service.updateLimits(code, settings);

        return ResponseEntity.ok(PlanPresentationMapper.toResponseDTO(plan));
    }
}
