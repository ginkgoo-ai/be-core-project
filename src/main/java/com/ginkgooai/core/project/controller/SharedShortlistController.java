package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.constant.ContextsConstant;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.config.security.RequireShareShortlistScope;
import com.ginkgooai.core.project.domain.application.CommentType;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.GuestCommentCreateRequest;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.service.application.ShortlistService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shared-shortlists")
@RequiredArgsConstructor
@Tag(name = "Shared Shortlists Management", description = "APIs for managing shared shortlists")
public class SharedShortlistController {

    private final ShortlistService shortlistService;

    private final SubmissionService submissionService;

    @Operation(summary = "Guest(Producer) Get shortlist items by shortlist ID", description = "Retrieves a paginated list of items from a specific shortlist. "
            +
            "Requires ROLE_USER role or ROLE_GUEST role with appropriate shortlist scopes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved shortlist items", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this shortlist"),
            @ApiResponse(responseCode = "404", description = "Shortlist not found")
    })
    @GetMapping("/{shortlistId}/items")
    @RequireShareShortlistScope
    public ResponseEntity<Page<ShortlistItemResponse>> getShortlistItemsByShortlistId(
            @Parameter(description = "ID of the shortlist", required = true, example = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e") @PathVariable String shortlistId,
            @Parameter(description = "Optional search keyword to filter items", example = "John Smith") @RequestParam(required = false) String keyword,
            @Parameter(description = "Pagination parameters") @ParameterObject Pageable pageable) {

        return ResponseEntity
                .ok(shortlistService.listShortlistItemsByShortlistId(shortlistId, keyword, pageable));
    }

    @Operation(summary = "Guest(Producer) Add comment to submission", description = "Adds a new comment to an existing submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment added successfully", content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    @PostMapping("/{shortlistId}/items/{itemId}/submissions/{submissionId}/comments")
    @RequireShareShortlistScope
    public ResponseEntity<SubmissionResponse> addComment(
            @Parameter(description = "ID of the shortlist", required = true, example = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e") @PathVariable String shortlistId,
            @Parameter(description = "ID of the submission", required = true, example = "submission_123") @PathVariable String submissionId,
            @Valid @RequestBody GuestCommentCreateRequest request) {
        return ResponseEntity.ok(submissionService.addComment(
                submissionId,
                ContextUtils.getWorkspaceId(),
                CommentCreateRequest.builder()
                        .content(request.getContent())
                        .type(CommentType.PUBLIC)
                        .parentCommentId(request.getParentCommentId())
                        .build(),
                ContextUtils.get(ContextsConstant.USER_EMAIL, String.class, "unknown")));
    }

}