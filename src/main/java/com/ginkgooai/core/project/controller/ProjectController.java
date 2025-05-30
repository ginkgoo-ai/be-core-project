package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.dto.request.ProjectCreateRequest;
import com.ginkgooai.core.project.dto.request.ProjectUpdateRequest;
import com.ginkgooai.core.project.dto.request.ProjectUpdateStatusRequest;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.dto.response.ProjectListResponse;
import com.ginkgooai.core.project.dto.response.ProjectResponse;
import com.ginkgooai.core.project.dto.response.ProjectStatisticsResponse;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import com.ginkgooai.core.project.service.ProjectReadService;
import com.ginkgooai.core.project.service.ProjectWriteService;
import com.ginkgooai.core.project.service.application.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@Tag(name = "Project Management", description = "APIs for managing projects")
@Slf4j
public class ProjectController {

    @Autowired
    private ProjectReadService projectReadService;

    @Autowired
    private ProjectWriteService projectWriteService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ActivityLoggerService activityLogger;

    @Operation(summary = "Create a new project", description = "Creates a new project with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectResponse project = projectWriteService.createProject(request);

        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a project by ID", description = "Retrieves details of a specific project by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable String id) {
        return projectReadService.findById(ContextUtils.getWorkspaceId(), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Get paginated list of projects", description = "Retrieves a paginated list of projects with optional filtering by name (fuzzy search), status, and sorting by updated date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of projects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
	public ResponseEntity<Page<ProjectListResponse>> getProjects(
            @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
			@Parameter(description = "Sort field (name, updatedAt, roleCount, pendingReviewCount)",
					example = "updatedAt") @RequestParam(defaultValue = "updatedAt") String sortField,
            @Parameter(description = "Filter by project name (fuzzy search)", example = "Enchanted") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by project status (e.g., DRAFTING, ACTIVE, COMPLETED, PENDING_REVIEW)", example = "IN_PROGRESS") @RequestParam(required = false) ProjectStatus status) {

        // Create Pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

		Page<ProjectListResponse> projects = projectReadService.findProjectList(name, status, pageable);

        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @Operation(summary = "Get basic info of all projects", description = "Retrieves basic information (id and name) of all projects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of project basic info retrieved successfully")
    })
    @GetMapping("/basic")
    public ResponseEntity<List<ProjectBasicResponse>> getAllProjectsBasicInfo() {
        List<ProjectBasicResponse> projects = projectReadService.findAllBasicInfo();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @Operation(summary = "Update a project", description = "Updates an existing project with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable String id,
                                                         @RequestBody ProjectUpdateRequest request) {
        Project updatedProject = projectWriteService.updateProject(id, request, ContextUtils.getWorkspaceId());
        return new ResponseEntity<>(ProjectResponse.from(updatedProject), HttpStatus.OK);
    }

    @Operation(summary = "Update project status", description = "Updates the status of an existing project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable String id,
            @Parameter(description = "New project status", required = true, schema = @Schema(implementation = ProjectStatus.class, example = "IN_PROGRESS")) @RequestBody ProjectUpdateStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Project updatedProject = projectWriteService.updateProjectStatus(id, request.getStatus());

        return new ResponseEntity<>(ProjectResponse.from(updatedProject), HttpStatus.OK);
    }

    @Operation(summary = "Delete a project", description = "Deletes a project by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectWriteService.deleteProject(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get project statistics", description = "Retrieves statistics for a specific project.")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    public ResponseEntity<ProjectStatisticsResponse> getProjectsStatistics() {
        ProjectStatisticsResponse statistics = projectReadService.getProjectsStatistics();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }
}