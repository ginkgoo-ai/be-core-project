package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.common.utils.IpUtils;
import com.ginkgooai.core.project.config.security.RequireShareShortlistScope;
import com.ginkgooai.core.project.domain.application.CommentType;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.GuestCommentCreateRequest;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.BatchShareShortlistResponse;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.ShortlistShareResponse;
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
import jakarta.servlet.http.HttpServletRequest;
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

	private final SubmissionService submissionService;

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
				example = "updatedAt") @RequestParam(defaultValue = "createdAt") String sortField) {

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

	@Operation(summary = "Guest(Producer) Get shortlist items by shortlist ID", description = "Retrieves a paginated list of items from a specific shortlist. "
		+
		"Requires ROLE_USER role or ROLE_PRODUCER role with appropriate shortlist scopes.")
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
		@Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
		@Parameter(description = "Sort direction (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
		@Parameter(description = "Sort field (e.g., updatedAt)", example = "updatedAt") @RequestParam(defaultValue = "createdAt") String sortField) {
		Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseEntity
			.ok(shortlistService.listShortlistItemsByShortlistId(shortlistId, keyword, pageable));
	}

	@Operation(summary = "Get shortlist item by ID", description = "Retrieves details of a specific shortlist item by its ID. "
		+
		"Requires ROLE_PRODUCER/ROLE_USER role with appropriate shortlist scopes.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved shortlist item details", content = @Content(schema = @Schema(implementation = ShortlistItemResponse.class))),
		@ApiResponse(responseCode = "403", description = "Not authorized to view this shortlist item"),
		@ApiResponse(responseCode = "404", description = "Shortlist item not found")
	})
	@GetMapping("/{shortlistId}/items/{itemId}")
	@RequireShareShortlistScope
	public ResponseEntity<ShortlistItemResponse> getShortlistItemById(
		@Parameter(description = "ID of the shortlist", required = true, example = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e") @PathVariable String shortlistId,
		@Parameter(description = "ID of the shortlist item", required = true, example = "abc12345-1234-5678-90ab-1234567890ab") @PathVariable String itemId) {

		return ResponseEntity.ok(shortlistService.getShortlistItemById(itemId));
	}

	@Operation(summary = "Guest(Producer) Get submission details", description = "Retrieves detailed information about a specific submission including its comments")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Submission found", content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
		@ApiResponse(responseCode = "404", description = "Submission not found")
	})
	@GetMapping("/{shortlistId}/submissions/{submissionId}")
	@RequireShareShortlistScope
	public ResponseEntity<SubmissionResponse> getSubmission(
		@Parameter(description = "ID of the submission to retrieve", required = true, example = "submission_123") @PathVariable String submissionId) {
		return ResponseEntity.ok(submissionService.getSubmission(submissionId));
	}

	@Operation(summary = "Guest(Producer) record video view", description = "Records that a video has been viewed, incrementing its view counter. "
		+ "Requires ROLE_USER role or ROLE_PRODUCER role with appropriate shortlist scopes.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully recorded video view"),
		@ApiResponse(responseCode = "403", description = "Not authorized to view this submission"),
		@ApiResponse(responseCode = "404", description = "Submission not found")
	})
	@PostMapping("/{shortlistId}/submissions/{submissionId}/view")
	@RequireShareShortlistScope
	public ResponseEntity<?> recordVideoView(
		@Parameter(description = "ID of the shortlist", required = true, example = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e") @PathVariable String shortlistId,
		@Parameter(description = "ID of the submission", required = true, example = "afc08cb3-c88c-4191-9455-1ff73fe15c0f") @PathVariable String submissionId,
		HttpServletRequest request) {

		submissionService.incrementViewCount(submissionId, ContextUtils.getUserId(),
			IpUtils.getClientIpAddress(request));

		return ResponseEntity.ok().build();
	}


	@Operation(summary = "Guest(Producer) Add comment to submission", description = "Adds a new comment to an existing submission"
		+
		"Requires ROLE_PRODUCER role with appropriate shortlist scopes.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Comment added successfully", content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
		@ApiResponse(responseCode = "404", description = "Submission not found")
	})
	@PostMapping("/{shortlistId}/submissions/{submissionId}/comments")
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
			ContextUtils.getUserId()));
	}

}
