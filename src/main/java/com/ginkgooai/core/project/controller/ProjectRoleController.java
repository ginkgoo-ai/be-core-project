package com.ginkgooai.core.project.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ginkgooai.core.project.dto.response.RoleBasicResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectRolePatchRequest;
import com.ginkgooai.core.project.dto.request.ProjectRoleRequest;
import com.ginkgooai.core.project.dto.response.ProjectRoleResponse;
import com.ginkgooai.core.project.dto.response.ProjectRoleStatisticsResponse;
import com.ginkgooai.core.project.service.ProjectReadService;
import com.ginkgooai.core.project.service.ProjectWriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/projects")
@Tag(name = "Project Role Management", description = "APIs for managing project roles")
@Slf4j
public class ProjectRoleController {

    @Autowired
    private ProjectReadService projectReadService;

    @Autowired
    private ProjectWriteService projectWriteService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private StorageClient storageClient;

    @Operation(summary = "Create a new role for a project", description = "Creates a new role for the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{projectId}/roles")
    public ResponseEntity<ProjectRoleResponse> createRole(@PathVariable String projectId,
                                                          @RequestBody ProjectRoleRequest request) {
        ProjectRole role = projectWriteService.createRole(projectId, request);
        return new ResponseEntity<>(ProjectRoleResponse.from(role), HttpStatus.CREATED);
    }

    @Operation(summary = "Get a role by ID", description = "Retrieves details of a specific role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role found"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{projectId}/roles/{roleId}")
    public ResponseEntity<ProjectRoleResponse> getRoleById(@PathVariable String projectId,
                                                           @PathVariable String roleId) {
        return projectReadService.findRoleById(roleId)
                .map(role -> {
                    Map<String, CloudFileResponse> roleSideFilesMap = retrieveSideFilesForRole(
                            role);
                    return new ResponseEntity<>(ProjectRoleResponse.from(role, roleSideFilesMap),
                            HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Get all roles for a project", description = "Retrieves all roles associated with a specific project with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/{projectId}/roles")
    public ResponseEntity<Page<ProjectRoleResponse>> getProjectRoles(
            @PathVariable String projectId,
            @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Sort field (e.g., updatedAt)", example = "updatedAt") @RequestParam(defaultValue = "updatedAt") String sortField) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectRole> rolesPage = projectReadService.findRolesByProjectIdPaginated(projectId, pageable);

        List<String> allSideFileIds = rolesPage.getContent().stream()
                .filter(role -> role.getSides() != null)
                .flatMap(role -> Arrays.stream(role.getSides()))
                .filter(sideId -> sideId != null)
                .collect(Collectors.toList());

        Map<String, CloudFileResponse> sideFilesMap = Collections.emptyMap();
        if (!allSideFileIds.isEmpty()) {
            try {
                ResponseEntity<List<CloudFileResponse>> response = storageClient
                        .getFileDetails(allSideFileIds);
                if (response.getBody() != null) {
                    sideFilesMap = response.getBody().stream()
                            .collect(Collectors.toMap(file -> file.getId(),
                                    Function.identity()));
                }
            } catch (Exception e) {
                log.error("Error fetching side files: {}", e.getMessage());
            }
        }

        Map<String, CloudFileResponse> finalSideFilesMap = sideFilesMap;
        Page<ProjectRoleResponse> responseRolesPage = rolesPage
                .map(role -> ProjectRoleResponse.from(role, finalSideFilesMap));

        return new ResponseEntity<>(responseRolesPage, HttpStatus.OK);
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
        ProjectRole updatedRole = projectWriteService.updateRole(roleId, request);
        Map<String, CloudFileResponse> roleSideFilesMap = retrieveSideFilesForRole(updatedRole);
        return new ResponseEntity<>(
                ProjectRoleResponse.from(updatedRole, roleSideFilesMap),
                HttpStatus.OK);
    }

    @Operation(summary = "Update role details", description = "Partially updates specific fields (characterDescription, selfTapeInstructions, sides) of a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role details updated successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/{projectId}/roles/{roleId}/details")
    public ResponseEntity<ProjectRoleResponse> patchRoleDetails(
            @PathVariable String projectId,
            @PathVariable String roleId,
            @RequestBody ProjectRolePatchRequest request) {
        ProjectRole updatedRole = projectWriteService.patchRoleDetails(roleId, request);
        Map<String, CloudFileResponse> roleSideFilesMap = retrieveSideFilesForRole(updatedRole);
        return new ResponseEntity<>(
                ProjectRoleResponse.from(updatedRole, roleSideFilesMap),
                HttpStatus.OK);
    }

    @Operation(summary = "Delete a role", description = "Deletes a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @DeleteMapping("/{projectId}/roles/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable String projectId, @PathVariable String roleId) {
        projectWriteService.deleteRole(roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get role statistics", description = "Retrieves statistics about talents in different stages for a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{projectId}/roles/{roleId}/statistics")
    public ResponseEntity<ProjectRoleStatisticsResponse> getRoleStatistics(
            @Parameter(description = "ID of the project", required = true) @PathVariable String projectId,
            @Parameter(description = "ID of the role", required = true) @PathVariable String roleId) {
        ProjectRoleStatisticsResponse statistics = projectReadService.getRoleStatistics(roleId);

        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @Operation(summary = "Get all roles statistics for a project", description = "Retrieves statistics about talents for all roles in a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{projectId}/roles/statistics")
    public ResponseEntity<Page<ProjectRoleStatisticsResponse>> getProjectRolesStatistics(
            @Parameter(description = "ID of the project", required = true) @PathVariable String projectId,
            @Parameter(description = "Name for fuzzy search", example = "actor") @RequestParam(required = false) String name,
            @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "total") @RequestParam(required = false, defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "ASC") @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProjectRoleStatisticsResponse> statistics = projectReadService.getProjectRolesStatistics(projectId,
                name, pageable);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    private Map<String, CloudFileResponse> retrieveSideFilesForRole(ProjectRole role) {
        if (role == null || role.getSides() == null || role.getSides().length == 0) {
            return Collections.emptyMap();
        }

        try {
            ResponseEntity<List<CloudFileResponse>> response = storageClient
                    .getFileDetails(Arrays.asList(role.getSides()));
            return response.getBody() != null
                    ? response.getBody().stream()
                    .collect(Collectors.toMap(file -> file.getId(),
                            Function.identity()))
                    : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Error fetching side files for role {}: {}", role.getId(), e.getMessage());
            return Collections.emptyMap();
        }
    }

    // ProjectRoleController.java - add this method
    @Operation(summary = "Get basic information for all roles", description = "Retrieve basic metadata for all roles, for use in dropdowns etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved basic roles information")
    })
    @GetMapping("/roles/basic")
    public ResponseEntity<List<RoleBasicResponse>> getAllRolesBasicInfo() {
        List<RoleBasicResponse> basicInfo = projectReadService.findAllRolesBasicInfo();
        return new ResponseEntity<>(basicInfo, HttpStatus.OK);
    }
}