package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.service.application.ApplicationService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Application management endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Create new application",
            description = "Creates a new application for a talent applying to a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application created successfully",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Project, Role or Talent not found")
    })
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request) {
//        return ResponseEntity.ok(applicationService.createApplication(request));
        return null;
    }

    @Operation(summary = "Get application by ID",
            description = "Retrieves detailed information about a specific application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id) {
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(ApplicationResponse.from(application));
    }

    @Operation(summary = "List applications",
            description = "Retrieves a paginated list of applications with optional filtering")
    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> listApplications(
            @Parameter(description = "Project ID filter", example = "proj_12345")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Role ID filter", example = "role_12345")
            @RequestParam(required = false) String roleId,
            @Parameter(description = "Talent ID filter", example = "talent_12345")
            @RequestParam(required = false) String talentId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(applicationService.listApplications(projectId, roleId, talentId, pageable));
    }


    @Operation(summary = "Toggle shortlist status",
            description = "Adds or removes an application from the shortlist")
    @PatchMapping("/{id}/shortlist")
    public ResponseEntity<ApplicationResponse> toggleShortlist(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id,
            @Parameter(description = "Shortlist status", example = "true")
            @RequestParam boolean shortlisted) {
        return ResponseEntity.ok(applicationService.toggleShortlist(id, shortlisted));
    }

    @Operation(summary = "Add comment to application",
            description = "Adds a new comment to the application")
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApplicationResponse> addComment(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id,
            @Parameter(description = "Comment content", example = "Excellent performance in the audition")
            @RequestParam String content) {
        return ResponseEntity.ok(applicationService.addComment(id, null, content));
    }

    @Operation(summary = "Add note to application",
            description = "Adds a private note to the application")
    @PostMapping("/{id}/notes")
    public ResponseEntity<ApplicationResponse> addNote(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id,
            @Parameter(description = "Note content", example = "Internal: Follow up needed")
            @RequestParam String content) {
        return ResponseEntity.ok(applicationService.addNote(id, null, content));
    }
}