package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.ShortlistResponse;
import com.ginkgooai.core.project.service.application.ShortlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shortlists")
@RequiredArgsConstructor
public class ShortlistController {

    private final ShortlistService shortlistService;

    /**
     * Add a submission to the user's shortlist
     */
    @PostMapping("/items")
    public ResponseEntity<ShortlistResponse> addShortlistItem(
            @RequestParam String userId,
            @RequestParam String submissionId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(shortlistService.addShortlistItem(userId, submissionId, notes));
    }

    /**
     * List shortlist items with pagination and search
     */
    @GetMapping("/items")
    public ResponseEntity<Page<ShortlistItemResponse>> listShortlistItems(
            @RequestParam String projectId,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(shortlistService.listShortlistItems(projectId, keyword, pageable));
    }

    /**
     * Remove a submission from the shortlist
     */
    @DeleteMapping("/{shortlistId}/items/{submissionId}")
    public ResponseEntity<Void> removeShortlistItem(
            @PathVariable String shortlistId,
            @PathVariable String submissionId) {
        shortlistService.removeVideo(shortlistId, submissionId);
        return ResponseEntity.noContent().build();
    }
}