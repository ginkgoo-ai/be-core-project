package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.domain.talent.TalentComment;
import com.ginkgooai.core.project.dto.request.TalentCommentRequest;
import com.ginkgooai.core.project.dto.response.TalentCommentResponse;
import com.ginkgooai.core.project.repository.TalentCommentRepository;
import com.ginkgooai.core.project.repository.TalentRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TalentCommentService {

    private final TalentRepository talentRepository;
    private final TalentCommentRepository talentCommentRepository;
    private final ActivityLoggerService activityLogger;
    private final IdentityClient identityClient;

    /**
     * Add a comment to a talent
     */
    @Transactional
    public List<TalentCommentResponse> addComment(String workspaceId, String talentId,
                                                  String userId, TalentCommentRequest request) {
        // Verify talent exists and belongs to the workspace
        Talent talent = talentRepository.findById(talentId)
            .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", talentId));

        if (!talent.getWorkspaceId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Talent", "id", talentId);
        }

        // Create and save the comment
        TalentComment comment = TalentComment.builder().workspaceId(workspaceId).talentId(talentId)
            .content(request.getContent()).parentId(request.getParentId()).build();

        comment.setCreatedBy(userId);
        comment.setUpdatedBy(userId);

        TalentComment savedComment = talentCommentRepository.save(comment);

        // Get all comments for the talent with user info
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<TalentComment> comments =
            talentCommentRepository.findByWorkspaceIdAndTalentId(workspaceId, talentId, sort);

        // Get user info for all comment creators
        List<String> userIds = comments.stream().map(TalentComment::getCreatedBy).distinct()
            .collect(Collectors.toList());

        Map<String, UserInfoResponse> userInfoMap = identityClient.getUsersByIds(userIds).getBody()
            .stream().collect(Collectors.toMap(UserInfoResponse::getId, user -> user,
                (existing, replacement) -> existing));

        return comments.stream()
            .map(c -> TalentCommentResponse.from(c, userInfoMap.get(c.getCreatedBy())))
            .collect(Collectors.toList());
    }

    /**
     * Get all comments for a talent
     */
    public List<TalentCommentResponse> getComments(String workspaceId, String talentId) {
        // Verify talent exists and belongs to the workspace
        talentRepository.findById(talentId)
            .filter(talent -> talent.getWorkspaceId().equals(workspaceId))
            .orElseThrow(() -> new ResourceNotFoundException("Talent", "id", talentId));

        // Get comments sorted by creation time (newest first)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<TalentComment> comments =
            talentCommentRepository.findByWorkspaceIdAndTalentId(workspaceId, talentId, sort);

        if (ObjectUtils.isEmpty(comments)) {
            return new ArrayList<>();
        }

        // Get user info for all comment creators
        List<String> userIds = comments.stream().map(TalentComment::getCreatedBy).distinct()
            .collect(Collectors.toList());

        Map<String, UserInfoResponse> userInfoMap = identityClient.getUsersByIds(userIds).getBody()
            .stream().collect(Collectors.toMap(UserInfoResponse::getId, user -> user));

        return comments.stream().map(comment -> TalentCommentResponse.from(comment,
            userInfoMap.get(comment.getCreatedBy()))).collect(Collectors.toList());
    }

    /**
     * Delete a comment (soft delete by changing status)
     */
    @Transactional
    public void deleteComment(String workspaceId, String commentId, String userId) {
        // Find the comment and verify it belongs to the workspace
        TalentComment comment =
            talentCommentRepository.findByWorkspaceIdAndId(workspaceId, commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment",
                    "workspaceId:id", String.join(" ", workspaceId, commentId)));

        if (!comment.getWorkspaceId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }

        talentCommentRepository.delete(comment);
    }
}
