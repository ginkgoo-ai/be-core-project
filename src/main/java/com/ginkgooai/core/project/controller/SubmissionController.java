package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.constant.RedisKey;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private final SubmissionService submissionService;

    @Operation(summary = "Create submission for application",
            description = "Creates a new video submission for an existing application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission created successfully",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping()
    public ResponseEntity<SubmissionResponse> createSubmission(
            @Parameter(description = "Application ID", example = "app_12345")
            @PathVariable String id,
            @Valid @RequestBody SubmissionCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
        return ResponseEntity.ok(submissionService.createSubmission(workspaceId, id, request, jwt.getSubject()));
    }

    @GetMapping("/{submissionId}")
    @Operation(
            summary = "Get submission details by ID",
            description = "Retrieves detailed information about a specific submission including its comments"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Submission found and returned successfully",
            content = @Content(schema = @Schema(implementation = SubmissionResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Submission not found"
    )
    public ResponseEntity<SubmissionResponse> getSubmission(
            @Parameter(description = "ID of the submission to retrieve", required = true)
            @PathVariable String submissionId) {
        return ResponseEntity.ok(submissionService.getSubmission(submissionId));
    }

    @PostMapping("/{submissionId}/comments")
    public ResponseEntity<SubmissionResponse> addComment(
            @PathVariable String submissionId,
            @RequestParam String userId,
            @RequestBody String content) {
        return ResponseEntity.ok(submissionService.addComment(submissionId, userId, content));
    }

    @DeleteMapping("/{submissionId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String submissionId,
            @PathVariable String commentId,
            @RequestParam String userId) {
        submissionService.deleteComment(submissionId, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{submissionId}/comments")
    public ResponseEntity<List<SubmissionCommentResponse>> listComments(
            @PathVariable String submissionId) {
        return ResponseEntity.ok(submissionService.listComments(submissionId));
    }
}