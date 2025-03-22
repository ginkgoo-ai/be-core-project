package com.ginkgooai.core.project.controller;

import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.config.security.RequireShareShortlistScope;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.BatchShareShortlistResponse;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.service.application.ShortlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/shortlists")
@RequiredArgsConstructor
@Tag(name = "Shortlists", description = "APIs for managing shortlists")
public class ShortlistController {

        private final ShortlistService shortlistService;

        @Operation(summary = "Add item to shortlist", description = "Adds a submission to the user's shortlist with optional notes")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Item added to shortlist successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
                        @ApiResponse(responseCode = "404", description = "Submission not found"),
                        @ApiResponse(responseCode = "409", description = "Item already exists in shortlist")
        })
        @PostMapping("/items")
        public ResponseEntity addShortlistItem(
                        @Parameter(description = "ID of the submission to shortlist", required = true, example = "submission_456") @RequestParam String submissionId,
                        @Parameter(description = "Optional notes about the shortlisted item", example = "Great performance, consider for callback") @RequestParam(required = false) String notes) {
                shortlistService.addShortlistItem(ContextUtils.getUserId(), submissionId, notes);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "List shortlist items", description = "Retrieves a paginated list of shortlisted items with optional search functionality")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved shortlist items", content = @Content(schema = @Schema(implementation = Page.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
                        @ApiResponse(responseCode = "403", description = "Not authorized to view shortlist")
        })
        @GetMapping("/items")
        public ResponseEntity<Page<ShortlistItemResponse>> listShortlistItems(
                        @Parameter(description = "ID of the project to filter shortlist items", required = true, example = "proj_789") @RequestParam String projectId,
                        @Parameter(description = "Optional search keyword to filter items", example = "John Smith") @RequestParam(required = false) String keyword,
                        @Parameter(description = "Pagination parameters") @ParameterObject Pageable pageable) {
                return ResponseEntity.ok(shortlistService.listShortlistItems(projectId, keyword, pageable));
        }

        @Operation(summary = "Get shortlist items by shortlist ID", description = "Retrieves a paginated list of items from a specific shortlist. "
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

        @Operation(summary = "Remove item from shortlist", description = "Removes a specific submission from a shortlist")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Item successfully removed from shortlist"),
                        @ApiResponse(responseCode = "404", description = "Shortlist or submission not found"),
                        @ApiResponse(responseCode = "403", description = "Not authorized to modify shortlist")
        })
        @DeleteMapping("/items/{submissionId}")
        public ResponseEntity<Void> removeShortlistItem(
                        @Parameter(description = "ID of the shortlist", required = true, example = "shortlist_123") @PathVariable String shortlistId,
                        @Parameter(description = "ID of the submission to remove", required = true, example = "submission_456") @PathVariable String submissionId) {
                shortlistService.removeSubmission(submissionId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Share shortlist items", description = "Share selected shortlist items with external user via email")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Items shared successfully", content = @Content(schema = @Schema(implementation = BatchShareShortlistResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
                        @ApiResponse(responseCode = "404", description = "Items not found")
        })
        @PostMapping("/share")
        public ResponseEntity<BatchShareShortlistResponse> shareShortlist(
                        @RequestBody ShareShortlistRequest request) {
                Map<String, String> shareLinks = shortlistService.shareShortlist(request, ContextUtils.getUserId());
                return ResponseEntity.ok(BatchShareShortlistResponse.from(shareLinks));
        }
}