package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.*;
import com.ginkgooai.core.project.dto.request.*;
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
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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

    @Operation(summary = "Update role details",
            description = "Partially updates specific fields (characterDescription, selfTapeInstructions, sides) of a role")
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
        try {
            ProjectRole updatedRole = projectWriteService.patchRoleDetails(roleId, request);
            return new ResponseEntity<>(
                    ProjectRoleResponse.mapToProjectRoleResponse(updatedRole),
                    HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

    @Operation(summary = "Get role statistics",
            description = "Retrieves statistics about talents in different stages for a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{projectId}/roles/{roleId}/statistics")
    public ResponseEntity<ProjectRoleStatisticsResponse> getRoleStatistics(
            @Parameter(description = "ID of the project", required = true)
            @PathVariable String projectId,
            @Parameter(description = "ID of the role", required = true)
            @PathVariable String roleId) {
        try {
            ProjectRoleStatisticsResponse statistics = projectReadService.getRoleStatistics(roleId);

            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}