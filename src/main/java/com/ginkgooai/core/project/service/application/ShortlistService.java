package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.context.WorkspaceContext;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.Shortlist;
import com.ginkgooai.core.project.domain.application.ShortlistItem;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.dto.request.ShortlistItemAddRequest;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.ShortlistResponse;
import com.ginkgooai.core.project.repository.ShortlistRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortlistService {

    private final ShortlistRepository shortlistRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public ShortlistResponse addShortlistItem(String userId, String submissionId, String notes) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        Application application = submission.getApplication();
        
        // Try to find existing shortlist
        Shortlist shortlist = shortlistRepository.findByWorkspaceIdAndProjectIdAndOwnerId(application.getWorkspaceId(), application.getProjectId(), userId)
                .orElseGet(() -> {
                    // If shortlist doesn't exist, create a new one
                    Shortlist shortlistNew = Shortlist.builder()
                            .workspaceId(WorkspaceContext.getWorkspaceId())
                            .projectId(application.getProjectId())
                            .ownerId(userId)
                            .name("Shortlist")
                            .createdBy(userId)
                            .build();
                    return shortlistRepository.save(shortlistNew);
                });

        // Get the maximum order value
        Integer maxOrder = shortlist.getItems().stream()
                .map(ShortlistItem::getOrder)
                .max(Integer::compareTo)
                .orElse(0);

        ShortlistItem item = ShortlistItem.builder()
                .shortlist(shortlist)
                .submission(submission)
                .notes(notes)
                .order(maxOrder + 1)
                .addedBy(userId)
                .build();

        shortlist.getItems().add(item);
        return convertToResponse(shortlistRepository.save(shortlist));
    }

    @Transactional
    public void removeVideo(String shortlistId, String videoSubmissionId) {
        Shortlist shortlist = findShortlistById(shortlistId);
        shortlist.getItems().removeIf(item -> 
                item.getVideoSubmission().getId().equals(videoSubmissionId));
        shortlistRepository.save(shortlist);
    }

    @Transactional(readOnly = true)
    public Page<ShortlistResponse> listShortlists(String projectId, String roleId, Pageable pageable) {
        String workspaceId = WorkspaceContext.getWorkspaceId();

        return shortlistRepository.findAll(
                buildSpecification(workspaceId, projectId, roleId), 
                pageable
        ).map(this::convertToResponse);
    }

    private ShortlistResponse convertToResponse(Shortlist shortlist) {
        return ShortlistResponse.builder()
                .id(shortlist.getId())
                .workspaceId(shortlist.getWorkspaceId())
                .name(shortlist.getName())
                .description(shortlist.getDescription())
                .projectId(shortlist.getProjectId())
                .roleId(shortlist.getRoleId())
                .items(shortlist.getItems().stream()
                        .sorted(Comparator.comparing(ShortlistItem::getOrder))
                        .map(this::convertItemToResponse)
                        .collect(Collectors.toList()))
                .createdBy(shortlist.getCreatedBy())
                .createdAt(shortlist.getCreatedAt())
                .updatedAt(shortlist.getUpdatedAt())
                .build();
    }

    private ShortlistItemResponse convertItemToResponse(ShortlistItem item) {
        return ShortlistItemResponse.builder()
                .id(item.getId())
                .video(submissionService.getSubmissionResponse(item.getSubmission()))
                .notes(item.getNotes())
                .order(item.getOrder())
                .addedBy(item.getAddedBy())
                .addedAt(item.getAddedAt())
                .build();
    }
}