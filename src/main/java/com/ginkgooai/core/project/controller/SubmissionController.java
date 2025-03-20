package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.constant.RedisKey;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.SubmissionCommentResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.service.application.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission Management", description = "Endpoints for managing video submissions and their comments")
public class SubmissionController {

    private final SubmissionService submissionService;

    @Operation(summary = "Create new submission",
            description = "Creates a new video submission for an existing application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Submission created successfully",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid submission data"),
            @ApiResponse(responseCode = "404",
                    description = "Application not found")
    })
    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(
            @Valid @RequestBody SubmissionCreateRequest request) {
        return ResponseEntity.ok(submissionService.createSubmission(ContextUtils.getWorkspaceId(), request, ContextUtils.getUserId()));
    }

    @Operation(summary = "Get submission details",
            description = "Retrieves detailed information about a specific submission including its comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Submission found",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Submission not found")
    })
    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @Parameter(description = "ID of the submission to retrieve", required = true,
                    example = "submission_123")
            @PathVariable String submissionId) {
        return ResponseEntity.ok(submissionService.getSubmission(submissionId));
    }
    
    @Operation(summary = "Delete submission",
            description = "Deletes a submission and its associated comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Submission successfully deleted"),
            @ApiResponse(responseCode = "404",
                    description = "Submission not found"),
            @ApiResponse(responseCode = "403",
                    description = "Not authorized to delete submission")
    })
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(
            @Parameter(description = "ID of the submission to delete", required = true,
                    example = "submission_123")
            @PathVariable String submissionId,
            @AuthenticationPrincipal Jwt jwt) {
        submissionService.deleteSubmission(submissionId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add comment to submission",
            description = "Adds a new comment to an existing submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Comment added successfully",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Submission not found")
    })
    @PostMapping("/{submissionId}/comments")
    public ResponseEntity<SubmissionResponse> addComment(
            @Parameter(description = "ID of the submission", required = true,
                    example = "submission_123")
            @PathVariable String submissionId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.ok(submissionService.addComment(submissionId, ContextUtils.getWorkspaceId(), request, ContextUtils.getUserId()));
    }

    @Operation(summary = "Delete submission comment",
            description = "Removes a specific comment from a submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Comment successfully deleted"),
            @ApiResponse(responseCode = "404",
                    description = "Submission or comment not found"),
            @ApiResponse(responseCode = "403",
                    description = "Not authorized to delete comment")
    })
    @DeleteMapping("/{submissionId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID of the submission", required = true,
                    example = "submission_123")
            @PathVariable String submissionId,
            @Parameter(description = "ID of the comment to delete", required = true,
                    example = "comment_789")
            @PathVariable String commentId,
            @Parameter(description = "ID of the user deleting the comment", required = true,
                    example = "user_456")
            @RequestParam String userId) {
        submissionService.deleteComment(submissionId, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List submission comments",
            description = "Retrieves all comments for a specific submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Comments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404",
                    description = "Submission not found")
    })
    @GetMapping("/{submissionId}/comments")
    public ResponseEntity<List<SubmissionCommentResponse>> listComments(
            @Parameter(description = "ID of the submission", required = true,
                    example = "submission_123")
            @PathVariable String submissionId) {
        return ResponseEntity.ok(submissionService.listComments(submissionId));
    }
}