package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shortlists")
@RequiredArgsConstructor
@Tag(name = "Shortlists Management", description = "APIs for managing shortlists")
public class ShortlistController {

	private final ShortlistService shortlistService;

	@Operation(
		summary = "Add item to shortlist",
		description = "Adds a submission to the user's shortlist with optional notes")
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "Item added to shortlist successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input parameters"),
			@ApiResponse(responseCode = "404", description = "Submission not found"),
			@ApiResponse(responseCode = "409", description = "Item already exists in shortlist")
		})
	@PostMapping("/items")
	public ResponseEntity addShortlistItem(
		@Parameter(
			description = "ID of the submission to shortlist",
			required = true,
			example = "submission_456")
		@RequestParam
		String submissionId,
		@Parameter(
			description = "Optional notes about the shortlisted item",
			example = "Great performance, consider for callback")
		@RequestParam(required = false)
		String notes) {
		shortlistService.addShortlistItem(ContextUtils.getUserId(), submissionId, notes);
		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "List shortlist items",
		description =
			"Retrieves a paginated list of shortlisted items with optional search functionality")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Successfully retrieved shortlist items",
				content = @Content(schema = @Schema(implementation = Page.class))),
			@ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
			@ApiResponse(responseCode = "403", description = "Not authorized to view shortlist")
		})
	@GetMapping("/items")
	public ResponseEntity<Page<ShortlistItemResponse>> listShortlistItems(
		@Parameter(
			description = "ID of the project to filter shortlist items",
			required = true,
			example = "proj_789")
		@RequestParam
		String projectId,
		@Parameter(description = "Optional search keyword to filter items", example = "John Smith")
		@RequestParam(required = false)
		String keyword,
		@Parameter(description = "Pagination parameters") @ParameterObject Pageable pageable) {
		return ResponseEntity.ok(shortlistService.listShortlistItems(projectId, keyword, pageable));
	}

	@Operation(
		summary = "Remove item from shortlist",
		description = "Removes a specific submission from a shortlist")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "204",
				description = "Item successfully removed from shortlist"),
			@ApiResponse(responseCode = "404", description = "Shortlist or submission not found"),
			@ApiResponse(responseCode = "403", description = "Not authorized to modify shortlist")
		})
	@DeleteMapping("/items/{submissionId}")
	public ResponseEntity<Void> removeShortlistItem(
		@Parameter(
			description = "ID of the submission to remove",
			required = true,
			example = "submission_456")
		@PathVariable
		String submissionId) {
		shortlistService.removeSubmission(submissionId);
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "Share shortlist items",
		description =
			"Shares selected shortlist items with external users via email, generating unique access links")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Items shared successfully, returning access links for each recipient",
				content =
				@Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BatchShareShortlistResponse.class))),
			@ApiResponse(
				responseCode = "400",
				description = "Invalid input parameters or malformed request"),
			@ApiResponse(responseCode = "403", description = "Not authorized to share this shortlist"),
			@ApiResponse(responseCode = "404", description = "Shortlist or requested items not found")
		})
	@PostMapping("/{shortlistId}/share")
	public ResponseEntity<BatchShareShortlistResponse> shareShortlist(
		@Parameter(
			description = "ID of the shortlist to share",
			required = true,
			example = "shortlist_123")
		@PathVariable
		String shortlistId,
		@Parameter(
			description = "Share request details including recipient emails and optional message",
			required = true,
			schema = @Schema(implementation = ShareShortlistRequest.class))
		@RequestBody @Valid
		ShareShortlistRequest request) {
		Map<String, String> shareLinks = shortlistService.shareShortlist(request, shortlistId);
		return ResponseEntity.ok(BatchShareShortlistResponse.from(shareLinks));
	}
}
