package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.TalentRequest;
import com.ginkgooai.core.project.dto.request.TalentSearchRequest;
import com.ginkgooai.core.project.dto.response.TalentResponse;
import com.ginkgooai.core.project.service.application.TalentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talents")
@Tag(name = "Talent Management", description = "APIs for managing talent profiles and their information")
@RequiredArgsConstructor
public class TalentController {

    private final TalentService talentService;

    @Operation(summary = "Create new talent",
            description = "Creates a new talent profile by importing data from external profiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Talent profile created successfully",
                    content = @Content(schema = @Schema(implementation = TalentResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid talent data or profile information"),
            @ApiResponse(responseCode = "409",
                    description = "Talent profile already exists")
    })
    @PostMapping
    public ResponseEntity<TalentResponse> createTalent(
            @Parameter(description = "Talent creation details", required = true)
            @Valid @RequestBody TalentRequest request) {
        Talent talent = talentService.createTalentFromProfiles(request, ContextUtils.getWorkspaceId(), ContextUtils.getUserId());
        return ResponseEntity.ok(TalentResponse.from(talent));
    }

    @Operation(summary = "Update talent profiles",
            description = "Updates talent information by re-syncing with external profile sources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Talent profiles refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TalentResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Talent not found"),
            @ApiResponse(responseCode = "422",
                    description = "Unable to refresh profiles - external service error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TalentResponse> updateTalent(
            @Parameter(description = "ID of the talent to refresh", required = true, example = "talent_123")
            @PathVariable String id,
            @Valid @RequestBody TalentRequest request) {
        Talent talent = talentService.updateTalent(request, id);
        return ResponseEntity.ok(TalentResponse.from(talent));
    }

    @Operation(summary = "Get talent details",
            description = "Retrieves detailed information about a specific talent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Talent found",
                    content = @Content(schema = @Schema(implementation = TalentResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Talent not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TalentResponse> getTalent(
            @Parameter(description = "ID of the talent to retrieve", required = true,
                    example = "talent_123")
            @PathVariable String id) {
        TalentResponse talent = talentService.getTalentById(id);
        return ResponseEntity.ok(talent);
    }

    @Operation(summary = "Search talents",
            description = "Search for talents with various filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search parameters")
    })
    @GetMapping
    public ResponseEntity<Page<TalentResponse>> searchTalents(
            @Parameter(description = "Search criteria and filters")
            @Valid @ParameterObject TalentSearchRequest request,
            @Parameter(description = "Pagination parameters")
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(talentService.searchTalents(ContextUtils.getWorkspaceId(), request, pageable));
    }
}