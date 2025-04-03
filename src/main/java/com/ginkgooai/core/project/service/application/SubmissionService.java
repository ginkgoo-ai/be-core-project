package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.enums.Role;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.message.InnerMailSendMessage;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.common.utils.UrlUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.ShareCodeRequest;
import com.ginkgooai.core.project.client.identity.dto.ShareCodeResponse;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.*;
import com.ginkgooai.core.project.dto.request.CommentCreateRequest;
import com.ginkgooai.core.project.dto.request.InvitationEmailRequest;
import com.ginkgooai.core.project.dto.request.SubmissionCreateRequest;
import com.ginkgooai.core.project.dto.response.SubmissionCommentResponse;
import com.ginkgooai.core.project.dto.response.SubmissionResponse;
import com.ginkgooai.core.project.repository.*;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ginkgooai.core.common.constant.ContextsConstant.USER_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {

    @Value("${SHARE_LINK_EXPIRATION_TIMES:168}")
    private Integer shareLinkExpirationTimes;

    @Value(("${SLATE_URI:}"))
    private String slateUri;

    private final ApplicationRepository applicationRepository;

    private final SubmissionRepository submissionRepository;

    private final ShortlistItemRepository shortlistItemRepository;

    private final SubmissionCommentRepository submissionCommentRepository;

    private final SubmissionViewRecordRepository viewRecordRepository;

    private final StorageClient storageClient;

    private final IdentityClient identityClient;

    private final ActivityLoggerService activityLogger;

    private final SendEmailInnerService sendEmailInnerService;

    @Transactional
    public SubmissionResponse createSubmission(String workspaceId,
            SubmissionCreateRequest request,
            String userId) {

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId()));

        List<CloudFileResponse> videoFiles = storageClient.getFileDetails(Arrays.asList(request.getVideoId()))
                .getBody();
        log.debug("Video files: {}", videoFiles);

        CloudFileResponse video = videoFiles.stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Video file", "id", request.getVideoId()));
        Submission submission = Submission.builder()
                .workspaceId(workspaceId)
                .application(application)
                .videoName(video.getOriginalName())
                .videoUrl(video.getStoragePath())
                .videoDuration(video.getVideoDuration())
                .videoThumbnailUrl(video.getVideoThumbnailUrl())
                .videoResolution(video.getVideoResolution())
                .mimeType(video.getFileType())
                .build();
        Submission savedSubmission = submissionRepository.save(submission);

        activityLogger.log(
                workspaceId,
                application.getProject().getId(),
                application.getId(),
                ActivityType.SUBMISSION_ADDED_TO_SHORTLIST,
                Map.of(
                    "user", userId,
                    "talentName", String.join(" ", application.getTalent().getName()),
                    "videoName", submission.getVideoName()),
                null,
                userId);

        application.getTalent().incrementSubmissionCount();
        applicationRepository.save(application);

        return SubmissionResponse.from(savedSubmission, Collections.EMPTY_LIST, userId);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmission(String submissionId) {
        Submission submission = findSubmissionById(submissionId);

        List<String> commentUserIds = CollectionUtils.emptyIfNull(submission.getComments()).stream()
                .map(SubmissionComment::getCreatedBy)
                .distinct()
                .toList();

        List<UserInfoResponse> commentUsers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(commentUserIds)) {
            commentUsers = identityClient
                    .getUsersByIds(
                            submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                    .getBody();
        }
        return SubmissionResponse.from(submission, commentUsers, ContextUtils.get(USER_ID, String.class, null));
    }

    @Transactional
    public void deleteSubmission(String submissionId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        if (!submission.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("Not authorized to delete this submission");
        }

        List<ShortlistItem> shortlistItems = shortlistItemRepository.findAllBySubmissionId(submissionId, userId);
        for (ShortlistItem item : shortlistItems) {
            item.getSubmissions().remove(submission);
            if (item.getSubmissions().isEmpty()) {
                shortlistItemRepository.delete(item);
            } else {
                shortlistItemRepository.save(item);
            }
        }

        submissionRepository.delete(submission);
       
        submission.getApplication().getTalent().decrementSubmissionCount();
        applicationRepository.save(submission.getApplication());

        log.info("Deleted submission: {}", submissionId);
    }

    public SubmissionResponse addComment(String submissionId, String workspaceId,
            CommentCreateRequest request, String userId) {
        Submission submission = findSubmissionById(submissionId);
        SubmissionComment comment = SubmissionComment.builder()
                .submission(submission)
                .content(request.getContent())
                .type(request.getType())
                .workspaceId(workspaceId)
                .build();

        if (request.getParentCommentId() != null) {
            SubmissionComment parentComment = submissionCommentRepository
                    .findById(request.getParentCommentId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Parent comment", "id", request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        submission.getComments().add(comment);
        submissionRepository.save(submission);

        // Check if userId is an email (contains '@' character) before logging activity
        if (userId != null && userId.contains("@") && request.getType() == CommentType.PUBLIC) {
            activityLogger.log(
                submission.getWorkspaceId(),
                submission.getApplication().getProject().getId(),
                submission.getApplication().getId(),
                ActivityType.PRODUCER_FEEDBACK_ADDED,
                Map.of(
                    "talentName", submission.getApplication().getTalent().getName()),
                null,
                userId);
        }

        List<UserInfoResponse> users = identityClient
                .getUsersByIds(
                        submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                .getBody();
        return SubmissionResponse.from(submission, users, ContextUtils.getUserId());
    }

    @Transactional
    public void deleteComment(String submissionId, String commentId, String userId) {
        Submission submission = findSubmissionById(submissionId);

        submission.getComments()
                .removeIf(comment -> comment.getId().equals(commentId) && comment.getCreatedBy().equals(userId));

        submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionCommentResponse> listComments(String submissionId) {
        Submission submission = findSubmissionById(submissionId);

        Map<String, UserInfoResponse> usersMap = identityClient
                .getUsersByIds(
                        submission.getComments().stream().map(SubmissionComment::getCreatedBy).distinct().toList())
                .getBody().stream().collect(Collectors.toMap(UserInfoResponse::getId, user -> user));

        return submission.getComments().stream()
                .sorted(Comparator.comparing(SubmissionComment::getCreatedAt))
                .map(t -> SubmissionCommentResponse.from(t, usersMap.get(t.getCreatedBy())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(String submissionId, String userId, String ipAddress) {
        Submission submission = findSubmissionById(submissionId);

        if (shouldCountView(submission, userId, ipAddress)) {
            SubmissionViewRecord viewRecord = SubmissionViewRecord.builder()
                    .submission(submission)
                    .workspaceId(submission.getWorkspaceId())
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .build();

            viewRecordRepository.save(viewRecord);

            // Atomically increment view count
            if (submission.getViewCount() == null) {
                submission.setViewCount(1L);
            } else {
                submission.setViewCount(submission.getViewCount() + 1);
            }

            submissionRepository.save(submission);
            log.debug("Recorded video view: submissionId={}, newCount={}, user={}, ip={}",
                    submissionId, submission.getViewCount(), userId, ipAddress);
        } else {
            log.debug("Skipped view count: submissionId={}, user={}, ip={} (already viewed)",
                    submissionId, userId, ipAddress);
        }
    }

    /**
     * Determines if a view should be counted.
     * Simple deduplication logic - each user only counts once per video
     */
    private boolean shouldCountView(Submission submission, String userId, String ipAddress) {
        // If both are empty, default to count
        if ((userId == null || userId.isEmpty()) && (ipAddress == null || ipAddress.isEmpty())) {
            return true;
        }

        // Check if this user/IP has already viewed this submission
        boolean hasExistingView = viewRecordRepository.existsBySubmissionIdAndUserIdOrIpAddress(
                submission.getId(), userId);

        // Only count if no previous view record exists
        return !hasExistingView;
    }

    private Submission findSubmissionById(String id) {
        String workspaceId = ContextUtils.getWorkspaceId();

        return submissionRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)))
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
    }

    /**
     * Sends invitation emails to multiple applicants for their submissions.
     * This method processes a batch of applications and sends personalized invitation emails
     * to each applicant using the specified email template.
     *
     * The email template will be populated with the following placeholders:
     * - ROLE_NAME: The name of the role the applicant is applying for
     * - PROJECT_NAME: The name of the project
     * - FIRST_NAME: The applicant's first name
     * - SENDER_NAME: The name of the user sending the invitation
     *
     * @param request The invitation email request containing:
     *                - emailTemplateType: The type of email template to use
     *                - applicationIds: List of application IDs to send invitations for
     * @throws ResourceNotFoundException if any of the specified applications are not found
     */
    public void sendInvitationEmail(InvitationEmailRequest request) {
        List<Application> applications = applicationRepository.findAllById(request.getApplicationIds());

        if (CollectionUtils.isEmpty(applications)) {
            throw new ResourceNotFoundException("Application", "ids", request.getApplicationIds());
        }

        UserInfoResponse userInfoResponse = identityClient.getUserById(ContextUtils.getUserId())
                .getBody();

        if (userInfoResponse == null) {
            throw new ResourceNotFoundException("User", "id", ContextUtils.getUserId());
        }

        String baseUrl = slateUri + "/shares/application";
        List<InnerMailSendMessage.Receipt> list = applications.stream().map(application -> {

            ShareCodeResponse response =
                    identityClient
                            .generateShareCode(
                                    ShareCodeRequest.builder()
                                            .workspaceId(ContextUtils.getWorkspaceId())
                                            .resource("application")
                                            .resourceId(application.getId())
                                            .guestName( application.getTalent().getName())
                                            .guestEmail(application.getTalent().getEmail())
                                            .roles(List.of(Role.ROLE_TALENT))
                                            .write(true)
                                            .expiryHours(shareLinkExpirationTimes)
                                            .build())
                            .getBody();

            String shareLink = UrlUtils.appendQueryParam(baseUrl + "/" + application.getId(), "share_code", response.getShareCode());

            Map<String, String> placeholders = Map.of(
                    "ROLE_NAME", application.getRole().getName(),
                    "PROJECT_NAME", application.getProject().getName(),
                "FIRST_NAME", application.getTalent().getName(),
                    "SENDER_NAME", userInfoResponse.getFirstName() + " " + userInfoResponse.getLastName(),
                        "SHARE_LINK",shareLink
            );
            return InnerMailSendMessage.Receipt.builder()
                    .placeholders(placeholders)
                    .to(application.getTalent().getEmail()).build();
        }).toList();

        sendEmailInnerService.email(InnerMailSendMessage.builder()
                .emailTemplateType(request.getEmailTemplateType())
                .receipts(list).build());
    }
}
