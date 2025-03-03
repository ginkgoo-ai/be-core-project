package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.constant.RedisKey;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.dto.request.ApplicationCreateRequest;
import com.ginkgooai.core.project.dto.response.ApplicationCommentResponse;
import com.ginkgooai.core.project.dto.response.ApplicationNoteResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Application management endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
            @Valid @RequestBody ApplicationCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
        return ResponseEntity.ok(applicationService.createApplication(request, workspaceId, jwt.getSubject()));
    }

    @Operation(summary = "Get application by ID",
            description = "Retrieves detailed information about a specific application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@Parameter(description = "Application ID", example = "app_12345")
                                                              @PathVariable String id,
                                                              @AuthenticationPrincipal Jwt jwt) {
        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
        Application application = applicationService.getApplicationById(workspaceId, id);
        return ResponseEntity.ok(ApplicationResponse.from(application));
    }

    @Operation(summary = "List applications",
            description = "Retrieves a paginated list of applications with filtering and sorting options")
    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> listApplications(
            @Parameter(description = "Project ID filter")
            @RequestParam(required = false) String projectId,
            @Parameter(description = "Role ID filter")
            @RequestParam(required = false) String roleId,
            @Parameter(description = "Search keyword for talent name or email or role name")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by application status")
            @RequestParam(required = false) ApplicationStatus status,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {

        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
        return ResponseEntity.ok(applicationService.listApplications(workspaceId, projectId, roleId, keyword, status, pageable));
    }

    @Operation(summary = "Add comment to application",
            description = "Adds a new comment to the application")
    @PostMapping("/{id}/comments")
    public ResponseEntity<List<ApplicationCommentResponse>> addComment(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id,
            @Parameter(description = "Comment content", example = "Excellent performance in the audition")
            @RequestParam String content,
            @AuthenticationPrincipal Jwt jwt) {
        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
        return ResponseEntity.ok(applicationService.addComment(workspaceId, id, jwt.getSubject(), content));
    }

    @Operation(summary = "Add note to application",
            description = "Adds a private note to the application")
    @PostMapping("/{id}/notes")
    public ResponseEntity<List<ApplicationNoteResponse>> addNote(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id,
            @Parameter(description = "Note content", example = "Internal: Follow up needed")
            @RequestParam String content,
            @AuthenticationPrincipal Jwt jwt) {
        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
        return ResponseEntity.ok(applicationService.addNote(workspaceId, id, jwt.getSubject(), content));
    }
}