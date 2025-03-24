package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

        @Operation(summary = "Guest(Producer) Add comment to submission", description = "Adds a new comment to an existing submission"
                        +
                        "Requires ROLE_GUEST role with appropriate shortlist scopes.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment added successfully", content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Submission not found")
        })
        @PostMapping("/{shortlistId}/items/{itemId}/submissions/{submissionId}/comments")
        @RequireShareShortlistScope
        public ResponseEntity<SubmissionResponse> addComment(
                        @Parameter(description = "ID of the shortlist", required = true, example = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e") @PathVariable String shortlistId,
                        @Parameter(description = "ID of the submission", required = true, example = "afc08cb3-c88c-4191-9455-1ff73fe15c0f") @PathVariable String submissionId,
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

        @Operation(summary = "Get shortlist item by ID", description = "Retrieves details of a specific shortlist item by its ID. "
                        +
                        "Requires ROLE_GUEST role with appropriate shortlist scopes.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved shortlist item details", content = @Content(schema = @Schema(implementation = ShortlistItemResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Not authorized to view this shortlist item"),
                        @ApiResponse(responseCode = "404", description = "Shortlist item not found")
        })
        @GetMapping("/{shortlistId}/items/{itemId}")
        @RequireShareShortlistScope
        public ResponseEntity<ShortlistItemResponse> getShortlistItemById(
                        @Parameter(description = "ID of the shortlist item", required = true, example = "abc12345-1234-5678-90ab-1234567890ab") @PathVariable String itemId) {

                return ResponseEntity.ok(shortlistService.getShortlistItemById(itemId));
        }

        @Operation(summary = "Record video view", description = "Records that a video has been viewed, incrementing its view counter. "
                        + "Requires ROLE_USER role or ROLE_GUEST role with appropriate shortlist scopes.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully recorded video view"),
                        @ApiResponse(responseCode = "403", description = "Not authorized to view this submission"),
                        @ApiResponse(responseCode = "404", description = "Submission not found")
        })
        @PostMapping("/{shortlistId}/items/{itemId}/submissions/{submissionId}/view")
        @RequireShareShortlistScope
        public ResponseEntity<?> recordVideoView(
                        @Parameter(description = "ID of the shortlist", required = true, example = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e") @PathVariable String shortlistId,
                        @Parameter(description = "ID of the shortlist item", required = true, example = "abc12345-1234-5678-90ab-1234567890ab") @PathVariable String itemId,
                        @Parameter(description = "ID of the submission", required = true, example = "afc08cb3-c88c-4191-9455-1ff73fe15c0f") @PathVariable String submissionId,
                        HttpServletRequest request) {

                submissionService.incrementViewCount(submissionId, ContextUtils.getUserId(),
                                IpUtils.getClientIpAddress(request));

                return ResponseEntity.ok().build();
        }

}