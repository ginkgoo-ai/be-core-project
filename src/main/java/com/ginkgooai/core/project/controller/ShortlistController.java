package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.BatchShareShortlistResponse;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.ShortlistShareResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shortlists")
@RequiredArgsConstructor
@Tag(name = "Shortlists Management", description = "APIs for managing shortlists")
public class ShortlistController {

	private final ShortlistService shortlistService;

	@Operation(summary = "Add item to shortlist",
			description = "Adds a submission to the user's shortlist with optional notes")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Item added to shortlist successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input parameters"),
			@ApiResponse(responseCode = "404", description = "Submission not found"),
			@ApiResponse(responseCode = "409", description = "Item already exists in shortlist") })
	@PostMapping("/items")
	public ResponseEntity addShortlistItem(
			@Parameter(description = "ID of the submission to shortlist", required = true,
					example = "submission_456") @RequestParam String submissionId,
			@Parameter(description = "Optional notes about the shortlisted item",
					example = "Great performance, consider for callback") @RequestParam(
							required = false) String notes) {
		shortlistService.addShortlistItem(ContextUtils.getUserId(), submissionId, notes);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "List shortlist items",
			description = "Retrieves a paginated list of shortlisted items with optional search functionality")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved shortlist items",
					content = @Content(schema = @Schema(implementation = Page.class))),
			@ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
			@ApiResponse(responseCode = "403", description = "Not authorized to view shortlist") })
	@GetMapping("/items")
	public ResponseEntity<Page<ShortlistItemResponse>> listShortlistItems(
			@Parameter(description = "ID of the project to filter shortlist items", required = true,
					example = "proj_789") @RequestParam String projectId,
			@Parameter(description = "Optional search keyword to filter items",
					example = "John Smith") @RequestParam(required = false) String keyword,
			@Parameter(description = "Page number (zero-based)",
					example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Sort direction (ASC/DESC)",
					example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
			@Parameter(description = "Sort field (e.g., updatedAt)",
					example = "updatedAt") @RequestParam(defaultValue = "updatedAt") String sortField) {

		Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseEntity.ok(shortlistService.listShortlistItems(projectId, keyword, pageable));
	}

	@Operation(summary = "Remove item from shortlist", description = "Removes a specific submission from a shortlist")
	@ApiResponses(
			value = { @ApiResponse(responseCode = "204", description = "Item successfully removed from shortlist"),
					@ApiResponse(responseCode = "404", description = "Shortlist or submission not found"),
					@ApiResponse(responseCode = "403", description = "Not authorized to modify shortlist") })
	@DeleteMapping("/items/{submissionId}")
	public ResponseEntity<Void> removeShortlistItem(@Parameter(description = "ID of the submission to remove",
			required = true, example = "submission_456") @PathVariable String submissionId) {
		shortlistService.removeSubmission(submissionId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Share shortlist items",
			description = "Shares selected shortlist items with external users via email, generating unique access links")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Items shared successfully, returning access links for each recipient",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BatchShareShortlistResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input parameters or malformed request"),
			@ApiResponse(responseCode = "403", description = "Not authorized to share this shortlist"),
			@ApiResponse(responseCode = "404", description = "Shortlist or requested items not found") })
	@PostMapping("/items/share")
	public ResponseEntity<BatchShareShortlistResponse> shareShortlist(
			@Parameter(description = "Share request details including recipient emails and optional message",
					required = true, schema = @Schema(
							implementation = ShareShortlistRequest.class)) @RequestBody @Valid ShareShortlistRequest request) {
		Map<String, String> shareLinks = shortlistService.shareShortlist(request);
		return ResponseEntity.ok(BatchShareShortlistResponse.from(shareLinks));
	}

	@Operation(summary = "Get shortlist shares", description = "Retrieves all active shares for a shortlist")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieved shortlist shares"),
			@ApiResponse(responseCode = "404", description = "Shortlist not found"),
			@ApiResponse(responseCode = "403", description = "Not authorized to view shortlist shares") })
	@GetMapping("/shares")
	public ResponseEntity<List<ShortlistShareResponse>> getShortlistShares(@Parameter(description = "ID of the project",
			required = true, example = "proj_789") @RequestParam String projectId) {
		List<ShortlistShareResponse> shares = shortlistService.getShortlistShares(projectId);
		return ResponseEntity.ok(shares);
	}

	@Operation(summary = "Revoke shortlist share", description = "Revokes access for a specific share")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Share successfully revoked"),
			@ApiResponse(responseCode = "404", description = "Share not found"),
			@ApiResponse(responseCode = "403", description = "Not authorized to revoke this share") })
	@DeleteMapping("/shares/{shareId}")
	public ResponseEntity<Void> revokeShortlistShare(@Parameter(description = "ID of the share to revoke",
			required = true, example = "share_123") @PathVariable String shareId) {
		shortlistService.revokeShortlistShare(shareId);
		return ResponseEntity.noContent().build();
	}

}
