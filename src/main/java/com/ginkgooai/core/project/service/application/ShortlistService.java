package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.context.WorkspaceContext;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.Shortlist;
import com.ginkgooai.core.project.domain.application.ShortlistItem;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.project.ProjectRole;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.ShortlistResponse;
import com.ginkgooai.core.project.repository.ShortlistItemRepository;
import com.ginkgooai.core.project.repository.ShortlistRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortlistService {

    private final ShortlistRepository shortlistRepository;
    private final ShortlistItemRepository shortlistItemRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public ShortlistResponse addShortlistItem(String userId, String submissionId, String notes) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        Application application = submission.getApplication();
        
        // Try to find existing shortlist
        Shortlist shortlist = shortlistRepository.findByWorkspaceIdAndProjectIdAndOwnerId(application.getWorkspaceId(), application.getProject().getId(), userId)
                .orElseGet(() -> {
                    // If shortlist doesn't exist, create a new one
                    Shortlist shortlistNew = Shortlist.builder()
                            .workspaceId(WorkspaceContext.getWorkspaceId())
                            .projectId(application.getProject().getId())
                            .ownerId(userId)
                            .name("Shortlist")
                            .createdBy(userId)
                            .build();
                    return shortlistRepository.save(shortlistNew);
                });

        // Get the maximum order value
        Integer maxOrder = shortlist.getItems().stream()
                .map(ShortlistItem::getSortOrder)
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
    public void removeVideo(String shortlistId, String submissionId) {
        Shortlist shortlist = findShortlistById(shortlistId);
        shortlist.getItems().removeIf(item -> 
                item.getSubmission().getId().equals(submissionId));
        shortlistRepository.save(shortlist);
    }

    private Shortlist findShortlistById(String shortlistId) {
        return shortlistRepository.findById(shortlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));
    }

    @Transactional(readOnly = true)
    public Page<ShortlistItemResponse> listShortlistItems(String projectId, String keyword, Pageable pageable) {
        String workspaceId = WorkspaceContext.getWorkspaceId();
        String userId = null;

        // First get the user's shortlist for this project
        Shortlist shortlist = shortlistRepository.findByWorkspaceIdAndProjectIdAndOwnerId(workspaceId, projectId, userId)
                .orElse(null);

        if (shortlist == null) {
            return Page.empty(pageable);
        }

        return shortlistItemRepository.findShortlistItems(
                buildShortlistItemSpecification(shortlist.getId(), keyword),
                pageable
        ).map(this::convertItemToResponse);
    }

    private Specification<ShortlistItem> buildShortlistItemSpecification(String shortlistId, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Basic condition - items belonging to the specific shortlist
            predicates.add(cb.equal(root.get("shortlist").get("id"), shortlistId));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";

                // Join with Submission and Application
                Join<ShortlistItem, Submission> submissionJoin = root.join("submission", JoinType.LEFT);
                Join<Submission, Application> applicationJoin = submissionJoin.join("application", JoinType.LEFT);
                // Join with ProjectRole and Talent
                Join<Application, ProjectRole> roleJoin = applicationJoin.join("role", JoinType.LEFT);
                Join<Application, Talent> talentJoin = applicationJoin.join("talent", JoinType.LEFT);

                // Create OR conditions for roleName and talentName
                Predicate roleNamePredicate = cb.like(
                        cb.lower(roleJoin.get("name")),
                        likePattern
                );
                Predicate talentNamePredicate = cb.like(
                        cb.lower(talentJoin.get("name")),
                        likePattern
                );

                predicates.add(cb.or(roleNamePredicate, talentNamePredicate));
            }

            // Sort by order
            query.orderBy(cb.asc(root.get("order")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ShortlistResponse convertToResponse(Shortlist shortlist) {
        return ShortlistResponse.builder()
                .id(shortlist.getId())
                .workspaceId(shortlist.getWorkspaceId())
                .name(shortlist.getName())
                .description(shortlist.getDescription())
                .projectId(shortlist.getProjectId())
//                .roleId(shortlist.getRole().getId())
                .items(shortlist.getItems().stream()
                        .sorted(Comparator.comparing(ShortlistItem::getSortOrder))
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
                .notes(item.getNotes())
                .order(item.getSortOrder())
                .addedBy(item.getAddedBy())
                .addedAt(item.getAddedAt())
                .build();
    }
}