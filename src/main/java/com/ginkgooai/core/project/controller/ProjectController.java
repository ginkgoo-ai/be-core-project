package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.common.constant.MessageQueue;
import com.ginkgooai.core.common.constant.RedisKey;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.message.ActivityLogMessage;
import com.ginkgooai.core.project.domain.*;
import com.ginkgooai.core.project.dto.request.*;
import com.ginkgooai.core.project.dto.response.*;
import com.ginkgooai.core.project.service.ProjectReadService;
import com.ginkgooai.core.project.service.ProjectWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
@Tag(name = "Project Management", description = "APIs for managing projects, roles, NDAs, members, and activities")
@Slf4j
public class ProjectController {

    @Autowired
    private ProjectReadService projectReadService;

    @Autowired
    private ProjectWriteService projectWriteService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Operation(summary = "Create a new project", description = "Creates a new project with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request, @AuthenticationPrincipal Jwt jwt) {

        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX  + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);

        Project project = projectWriteService.createProject(request, workspaceId);
        ProjectResponse response = projectReadService.findById(project.getId())
                .orElseThrow(() -> new RuntimeException("Project not found after creation"));
        try {
            RQueue<ActivityLogMessage> queue = redissonClient.getQueue(MessageQueue.ACTIVITY_LOG_QUEUE);
            ActivityLogMessage message = ActivityLogMessage.builder()
                    .workspaceId(project.getWorkspaceId())
                    .projectId(project.getId())
                    .activityType(ActivityType.PROJECT_CREATED.name())
                    .description("default")
                    .createdBy(jwt.getSubject())
//                    .context(buildContext(joinPoint, result))
                    .createdAt(LocalDateTime.now())
                    .build();
            queue.offer(message);
            log.debug("Activity log message enqueued successfully");
        } catch (Exception e) {
            log.error("Failed to enqueue activity log message", e);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a project by ID", description = "Retrieves details of a specific project by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable String id) {
        return projectReadService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Get paginated list of projects", description = "Retrieves a paginated list of projects with optional filtering by name (fuzzy search), status, and sorting by updated date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of projects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Sort field (e.g., updatedAt)", example = "updatedAt") @RequestParam(defaultValue = "updatedAt") String sortField,
            @Parameter(description = "Filter by project name (fuzzy search)", example = "Enchanted") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by project status (e.g., DRAFTING, ACTIVE, COMPLETED, PENDING_REVIEW)", example = "IN_PROGRESS") @RequestParam(required = false) ProjectStatus status) {

        try {
            // Create Pageable with sorting
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ProjectResponse> projects = projectReadService.findProjects(name, status, pageable);

            return new ResponseEntity<>(projects, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable String id, @RequestBody ProjectRequest request) {
        try {
            Project updatedProject = projectWriteService.updateProject(id, request);
            ProjectResponse response = projectReadService.findById(updatedProject.getId())
                    .orElseThrow(() -> new RuntimeException("Project not found after update"));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
            @Parameter(description = "New project status", required = true)
            @RequestBody String status,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            Project updatedProject = projectWriteService.updateProjectStatus(id, status);
            ProjectResponse response = projectReadService.findById(updatedProject.getId())
                    .orElseThrow(() -> new RuntimeException("Project not found after status update"));

            // Log activity to message queue
            try {
                RQueue<ActivityLogMessage> queue = redissonClient.getQueue(MessageQueue.ACTIVITY_LOG_QUEUE);
                ActivityLogMessage message = ActivityLogMessage.builder()
                        .workspaceId(updatedProject.getWorkspaceId())
                        .projectId(updatedProject.getId())
                        .activityType(ActivityType.PROJECT_STATUS_UPDATED.name())
                        .description("Project status changed to " + status)
                        .createdBy(jwt.getSubject())
                        .createdAt(LocalDateTime.now())
                        .build();
                queue.offer(message);
                log.debug("Activity log message enqueued successfully for status update");
            } catch (Exception e) {
                log.error("Failed to enqueue activity log message for status update", e);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Delete a project", description = "Deletes a project by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        try {
            projectWriteService.deleteProject(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Create a new role for a project", description = "Creates a new role for the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{projectId}/roles")
    public ResponseEntity<ProjectRoleResponse> createRole(@PathVariable String projectId, @RequestBody ProjectRoleRequest request) {
        ProjectRole role = projectWriteService.createRole(projectId, request);
        ProjectRoleResponse response = ProjectRoleResponse.mapToProjectRoleResponse(role);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Create a new NDA for a project", description = "Creates a new NDA for the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "NDA created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{projectId}/ndas")
    public ResponseEntity<ProjectNdaResponse> createNda(@PathVariable String projectId, @RequestBody ProjectNdaRequest request) {
        ProjectNda nda = projectWriteService.createNda(projectId, request);
        ProjectNdaResponse response = ProjectNdaResponse.mapToProjectNdaResponse(nda);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Add a member to a project", description = "Adds a new member to the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse> addMember(@PathVariable String projectId, @RequestBody ProjectMemberRequest request) {
        ProjectMember member = projectWriteService.addMember(projectId, request);
        ProjectMemberResponse response = ProjectMemberResponse.mapToProjectMemberResponse(member);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Log an activity for a project", description = "Logs a new activity for the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Activity logged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{projectId}/activities")
    public ResponseEntity<ProjectActivityResponse> logActivity(@PathVariable String projectId, @RequestBody ProjectActivityRequest request) {
        ProjectActivity activity = projectWriteService.logActivity(projectId, request);
        ProjectActivityResponse response = ProjectActivityResponse.mapToProjectActivityResponse(activity);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a role by ID", description = "Retrieves details of a specific role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role found"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{projectId}/roles/{roleId}")
    public ResponseEntity<ProjectRoleResponse> getRoleById(@PathVariable String projectId, @PathVariable String roleId) {
        return projectReadService.findRoleById(roleId)
                .map(role -> new ResponseEntity<>(ProjectRoleResponse.mapToProjectRoleResponse(role), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Get all roles for a project", description = "Retrieves all roles associated with a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{projectId}/roles")
    public ResponseEntity<List<ProjectRoleResponse>> getProjectRoles(@PathVariable String projectId) {
        List<ProjectRoleResponse> roles = projectReadService.findRolesByProjectId(projectId)
                .stream()
                .map(ProjectRoleResponse::mapToProjectRoleResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @Operation(summary = "Update a role", description = "Updates an existing role with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{projectId}/roles/{roleId}")
    public ResponseEntity<ProjectRoleResponse> updateRole(
            @PathVariable String projectId,
            @PathVariable String roleId,
            @RequestBody ProjectRoleRequest request) {
        try {
            ProjectRole updatedRole = projectWriteService.updateRole(roleId, request);
            return new ResponseEntity<>(
                    ProjectRoleResponse.mapToProjectRoleResponse(updatedRole),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete a role", description = "Deletes a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @DeleteMapping("/{projectId}/roles/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable String projectId, @PathVariable String roleId) {
        try {
            projectWriteService.deleteRole(roleId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}