package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.dto.request.TalentCommentRequest;
import com.ginkgooai.core.project.dto.response.TalentCommentResponse;
import com.ginkgooai.core.project.service.application.TalentCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/talents/{talentId}/comments")
@Tag(name = "Talent Comments", description = "APIs for managing talent comments")
@RequiredArgsConstructor
public class TalentCommentController {

    private final TalentCommentService talentCommentService;

    @Operation(summary = "Add comment to talent",
        description = "Adds a new comment to the talent profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment added successfully",
            content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "Talent not found"),
        @ApiResponse(responseCode = "400", description = "Invalid comment data")})
    @PostMapping
    public ResponseEntity<List<TalentCommentResponse>> addComment(
        @Parameter(description = "Talent ID", required = true) @PathVariable String talentId,
        @Parameter(description = "Comment details",
            required = true) @Valid @RequestBody TalentCommentRequest request) {

        List<TalentCommentResponse> responses = talentCommentService.addComment(
            ContextUtils.getWorkspaceId(), talentId, ContextUtils.getUserId(), request);

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get talent comments", description = "Retrieves all comments for a talent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "Talent not found")})
    @GetMapping
    public ResponseEntity<List<TalentCommentResponse>> getComments(
        @Parameter(description = "Talent ID", required = true) @PathVariable String talentId) {

        List<TalentCommentResponse> comments =
            talentCommentService.getComments(ContextUtils.getWorkspaceId(), talentId);

        return ResponseEntity.ok(comments);
    }


    @Operation(summary = "Delete talent comment",
        description = "Soft deletes a comment from a talent profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")})
    @DeleteMapping("/{commentId}")
    public ResponseEntity deleteComment(
        @Parameter(description = "Talent ID", required = true) @PathVariable String talentId,
        @Parameter(description = "Comment ID",
            required = true) @PathVariable String commentId) {

        talentCommentService.deleteComment(ContextUtils.getWorkspaceId(), commentId,
            ContextUtils.getUserId());

        return ResponseEntity.ok().build();
    }
}
