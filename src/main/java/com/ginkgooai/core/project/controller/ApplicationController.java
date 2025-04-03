package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.config.security.RequireApplicationReadScope;
import com.ginkgooai.core.project.config.security.RequireApplicationWriteScope;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.dto.request.ApplicationCreateRequest;
import com.ginkgooai.core.project.dto.request.NoteCreateRequest;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.ApplicationCommentResponse;
import com.ginkgooai.core.project.dto.response.ApplicationNoteResponse;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.service.application.ApplicationService;
import com.ginkgooai.core.project.service.application.SubmissionService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "APIs for managing talent applications")
public class ApplicationController {

        private final ApplicationService applicationService;

        private final SubmissionService submissionService;

        @Operation(summary = "Create new application", description = "Creates a new application for a talent applying to a specific role")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Application created successfully", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Project, Role or Talent not found")
        })
        @PostMapping
        public ResponseEntity<ApplicationResponse> createApplication(
                        @Valid @RequestBody ApplicationCreateRequest request) {
                return ResponseEntity.ok(applicationService.createApplication(request, ContextUtils.getWorkspaceId(),
                                ContextUtils.getUserId()));
        }

        @Operation(summary = "Get application by ID", description = "Retrieves detailed information about a specific application")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Application found", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Application not found")
        })
        @RequireApplicationReadScope
        @GetMapping("/{applicationId}")
        public ResponseEntity<ApplicationResponse> getApplication(
            @Parameter(description = "Application ID", example = "app_12345") @PathVariable String applicationId) {
                ApplicationResponse application = applicationService.getApplicationById(ContextUtils.getWorkspaceId(), applicationId);
                return ResponseEntity.ok(application);
        }

        @Operation(summary = "List applications", description = "Retrieves a paginated list of applications with filtering and sorting options")
        @GetMapping
        public ResponseEntity<Page<ApplicationResponse>> listApplications(
                        @Parameter(description = "Project ID filter") @RequestParam(required = false) String projectId,
                        @Parameter(description = "Role ID filter") @RequestParam(required = false) String roleId,
                        @Parameter(description = "Talent ID filter") @RequestParam(required = false) String talentId,
                        @Parameter(description = "Start date for submission creation (format: yyyy-MM-dd'T'HH:mm:ss)")
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime startDateTime,
                        @Parameter(description = "End date for submission creation (format: yyyy-MM-dd'T'HH:mm:ss)")
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime endDateTime,
                        @Parameter(description = "View Mode") @RequestParam(defaultValue = "readingList") String viewMode,
                        @Parameter(description = "Search keyword for talent name or email or role name") @RequestParam(required = false) String keyword,
                        @Parameter(description = "Filter by application status") @RequestParam(required = false) ApplicationStatus status,
                        @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
                        @Parameter(description = "Sort field (e.g., updatedAt)", example = "updatedAt") @RequestParam(defaultValue = "updatedAt") String sortField) {

                if (sortField.equals("name")) {
                        sortField = "talent.name";
                }
                
                Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(applicationService.listApplications(ContextUtils.getWorkspaceId(),
                                ContextUtils.getUserId(), projectId, roleId, talentId, startDateTime, endDateTime, viewMode, keyword, status, pageable));
        }

        @Operation(summary = "Add comment to application", description = "Adds a new comment to the application")
        @PostMapping("/{id}/comments")
        @Hidden
        public ResponseEntity<List<ApplicationCommentResponse>> addComment(
                        @Parameter(description = "Application ID", example = "app_12345") @PathVariable String id,
                        @Parameter(description = "Comment content", example = "Excellent performance in the audition") @RequestParam String content) {
                return ResponseEntity.ok(applicationService.addComment(ContextUtils.getWorkspaceId(), id,
                                ContextUtils.getUserId(), content));
        }

        @Operation(summary = "Add note to application", description = "Adds a private note to the application")
        @PostMapping("/{id}/notes")
        public ResponseEntity<List<ApplicationNoteResponse>> addNote(
                        @Parameter(description = "Application ID", example = "app_12345") @PathVariable String id,
                        @RequestBody NoteCreateRequest request) {
                return ResponseEntity.ok(applicationService.addNote(ContextUtils.getWorkspaceId(), id,
                                ContextUtils.getUserId(), request.getContent()));
        }


        @Operation(summary = "Guest(Talent) Create new submission", description = "Creates a new video submission for an existing application")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission created successfully", content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid submission data"),
            @ApiResponse(responseCode = "404", description = "Application not found")
        })
        @PostMapping("/{applicationId}/submissions")
        @RequireApplicationWriteScope
        public ResponseEntity<SubmissionResponse> createSubmission(
            @Valid @RequestBody SubmissionCreateRequest request) {
                return ResponseEntity.ok(submissionService.createSubmission(ContextUtils.getWorkspaceId(), request,
                    ContextUtils.getUserId()));
        }


        @Operation(summary = "Guest(Talent) Delete submission", description = "Deletes a submission and its associated comments")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Submission successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Submission not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete submission")
        })
        @RequireApplicationWriteScope
        @DeleteMapping("/{applicationId}/submissions/{submissionId}")
        public ResponseEntity<Void> deleteSubmission(
            @Parameter(description = "ID of the submission to delete", required = true, example = "submission_123") @PathVariable String submissionId,
            @AuthenticationPrincipal Jwt jwt) {
                submissionService.deleteSubmission(submissionId, jwt.getSubject());
                return ResponseEntity.noContent().build();
        }
}