package com.ginkgooai.core.project.service.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ginkgooai.core.common.constant.ContextsConstant;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.GuestCodeRequest;
import com.ginkgooai.core.project.client.identity.dto.GuestCodeResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.domain.application.OwnerType;
import com.ginkgooai.core.project.domain.application.Shortlist;
import com.ginkgooai.core.project.domain.application.ShortlistItem;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.repository.ShortlistItemRepository;
import com.ginkgooai.core.project.repository.ShortlistRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShortlistService {

        private final ShortlistRepository shortlistRepository;
        private final ShortlistItemRepository shortlistItemRepository;
        private final SubmissionRepository submissionRepository;
        private final IdentityClient identityClient;
        private final ActivityLoggerService activityLogger;
        private final String guestLoginUri;
        private final String appBaseUrl;

        public ShortlistService(
                        ShortlistRepository shortlistRepository,
                        ShortlistItemRepository shortlistItemRepository,
                        SubmissionRepository submissionRepository,
                        IdentityClient identityClient,
                        ActivityLoggerService activityLogger,
                        @Value("${spring.security.oauth2.guest_login_uri}") String guestLoginUri,
                        @Value("${app.base-uri}") String appBaseUrl) {
                this.shortlistRepository = shortlistRepository;
                this.shortlistItemRepository = shortlistItemRepository;
                this.submissionRepository = submissionRepository;
                this.identityClient = identityClient;
                this.activityLogger = activityLogger;
                this.guestLoginUri = guestLoginUri;
                this.appBaseUrl = appBaseUrl;
        }

        @Transactional
        public void addShortlistItem(String userId, String submissionId, String notes) {
                Submission submission = submissionRepository.findById(submissionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

                Application application = submission.getApplication();

                // Try to find existing shortlist
                Shortlist shortlist = shortlistRepository
                                .findByWorkspaceIdAndProjectIdAndOwnerId(application.getWorkspaceId(),
                                                application.getProject().getId(),
                                                userId)
                                .orElseGet(() -> {
                                        // If shortlist doesn't exist, create a new one
                                        Shortlist shortlistNew = Shortlist.builder()
                                                        .workspaceId(ContextUtils.get().getWorkspaceId())
                                                        .projectId(application.getProject().getId())
                                                        .ownerId(userId)
                                                        .ownerType(OwnerType.INTERNAL)
                                                        .name("Shortlist")
                                                        .createdBy(userId)
                                                        .build();
                                        return shortlistRepository.save(shortlistNew);
                                });

                // Get the maximum order value
                Integer maxOrder = CollectionUtils.isEmpty(shortlist.getItems()) ? 0
                                : shortlist.getItems().stream()
                                                .map(ShortlistItem::getSortOrder)
                                                .max(Integer::compareTo)
                                                .orElse(0);

                ShortlistItem shortlistItem = shortlistItemRepository
                                .findByApplicationIdAndShortlistId(application.getId(), shortlist.getId())
                                .orElseGet(() -> {
                                        // Create new shortlist item if not exists
                                        ShortlistItem newItem = ShortlistItem.builder()
                                                        .application(application)
                                                        .shortlist(shortlist)
                                                        .submissions(new ArrayList<>())
                                                        .createdBy(userId)
                                                        .sortOrder(maxOrder + 1)
                                                        .submissions(new ArrayList<>())
                                                        .build();
                                        return shortlistItemRepository.save(newItem);
                                });
                application.setStatus(ApplicationStatus.SHORTLISTED);

                // Add submission to existing shortlist item if not already present
                if (!shortlistItem.getSubmissions().contains(submission)) {
                        shortlistItem.getSubmissions().add(submission);
                        shortlistItemRepository.save(shortlistItem);
                        log.debug("Added submission {} to shortlist item {}", submissionId, shortlistItem.getId());
                }
        }

        @Transactional
        public void removeSubmission(String submissionId) {
                Submission submission = submissionRepository.findById(submissionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

                List<ShortlistItem> shortlistItems = shortlistItemRepository.findAllBySubmissionId(submissionId,
                                ContextUtils.getUserId());
                if (shortlistItems.size() < 1) {
                        throw new ResourceNotFoundException("ShortlistItem", "submissionId-userId",
                                        String.join("-", submissionId, ContextUtils.getUserId()));
                }

                ShortlistItem shortlistItem = shortlistItems.get(0);
                shortlistItem.getSubmissions().remove(submission);
                if (shortlistItem.getSubmissions().isEmpty()) {
                        shortlistItemRepository.delete(shortlistItem);
                        log.debug("Deleted empty shortlist item: {}", shortlistItem.getId());
                } else {
                        shortlistItemRepository.save(shortlistItem);
                        log.debug("Removed submission {} from shortlist item {}", submissionId, shortlistItem.getId());
                }
        }

        private Shortlist findShortlistById(String shortlistId) {
                return shortlistRepository.findById(shortlistId)
                                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));
        }

        @Transactional(readOnly = true)
        public Page<ShortlistItemResponse> listShortlistItems(String projectId, String keyword, Pageable pageable) {
                String workspaceId = ContextUtils.get().getWorkspaceId();
                String userId = ContextUtils.get(ContextsConstant.USER_ID, String.class, null);

                // First get the user's shortlist for this project
                Shortlist shortlist = shortlistRepository
                                .findByWorkspaceIdAndProjectIdAndOwnerId(workspaceId, projectId, userId)
                                .orElse(null);

                if (shortlist == null) {
                        return Page.empty(pageable);
                }

                return shortlistItemRepository.findAll(
                                buildShortlistItemSpecification(shortlist.getId(), keyword),
                                pageable).map(t -> ShortlistItemResponse.from(t, userId));
        }


        @Transactional(readOnly = true)
        public Page<ShortlistItemResponse> listShortlistItemsByShortlistId(String shortlistId, String keyword,
                        Pageable pageable) {
                Shortlist shortlist = shortlistRepository.findById(shortlistId)
                                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));

                return shortlistItemRepository.findAll(
                                buildShortlistItemSpecification(shortlistId, keyword),
                                pageable).map(t -> ShortlistItemResponse.from(t, shortlist.getOwnerId()));
        }

        private Specification<ShortlistItem> buildShortlistItemSpecification(String shortlistId, String keyword) {
                return (root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        // 基本条件 - 属于特定短名单的条目
                        predicates.add(cb.equal(root.get("shortlist").get("id"), shortlistId));

                        if (keyword != null && !keyword.trim().isEmpty()) {
                                String likePattern = "%" + keyword.toLowerCase() + "%";

                                List<Predicate> keywordPredicates = new ArrayList<>();

                                Join<ShortlistItem, Application> applicationJoin = root.join("application",
                                                JoinType.LEFT);

                                Join<Application, ProjectRole> roleJoin = applicationJoin.join("role", JoinType.LEFT);
                                keywordPredicates.add(cb.like(cb.lower(roleJoin.get("name")), likePattern));

                                Join<Application, Talent> talentJoin = applicationJoin.join("talent", JoinType.LEFT);
                                keywordPredicates.add(cb.like(cb.lower(talentJoin.get("name")), likePattern));
                                keywordPredicates.add(cb.like(cb.lower(talentJoin.get("email")), likePattern));

                                Join<ShortlistItem, Submission> submissionsJoin = root.join("submissions",
                                                JoinType.LEFT);
                                keywordPredicates.add(cb.like(cb.lower(submissionsJoin.get("videoName")), likePattern));

                                predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
                        }

                        query.distinct(true);

                        query.orderBy(cb.asc(root.get("sortOrder")));

                        return cb.and(predicates.toArray(new Predicate[0]));
                };
        }

        public Map<String, String> shareShortlist(ShareShortlistRequest request, String userId) {
                Map<String, String> shareLinks = new HashMap<>();
                String workspaceId = ContextUtils.get().getWorkspaceId();

                List<Submission> submissions = submissionRepository.findAllById(request.getSubmissionIds());
                if (submissions.isEmpty()) {
                        throw new ResourceNotFoundException("Submissions", "ids",
                                        request.getSubmissionIds().toString());
                }

                Submission tempSubmission = submissions.get(0);
                String projectId = tempSubmission.getApplication().getProject().getId();

                // Group submissions by application
                Map<Application, List<Submission>> submissionMap = submissions.stream()
                                .collect(Collectors.groupingBy(submission -> submission.getApplication()));

                for (ShareShortlistRequest.Recipient recipient : request.getRecipients()) {
                        Shortlist shortlist = shortlistRepository.save(
                                        Shortlist.builder()
                                                        .workspaceId(workspaceId)
                                                        .projectId(projectId)
                                                        .ownerId(recipient.getEmail())
                                                        .ownerType(OwnerType.EXTERNAL)
                                                        .name(recipient.getName() != null
                                                                        ? "ShareShortlist for " + recipient.getName()
                                                                        : "ShareShortlist")
                                                        .createdBy(userId)
                                                        .build());

                        AtomicInteger sortOrder = new AtomicInteger(1);
                        submissionMap.forEach((application, applicationSubmissions) -> {
                                ShortlistItem newItem = ShortlistItem.builder()
                                                .application(application)
                                                .shortlist(shortlist)
                                                .submissions(applicationSubmissions)
                                                .createdBy(userId)
                                                .sortOrder(sortOrder.getAndIncrement())
                                                .build();
                                shortlistItemRepository.save(newItem);
                        });

                        // change status of all applications to SHORTLISTED
                        submissionMap.keySet().stream().forEach((Application application) -> application
                                        .setStatus(ApplicationStatus.SHORTLISTED));

                        Integer expiryHours = request.getExpiresInDays() != null ? request.getExpiresInDays() * 24
                                        : 7 * 24;

                        GuestCodeResponse response = identityClient.generateGuestCode(GuestCodeRequest.builder()
                                        .workspaceId(workspaceId)
                                        .resource("shortlist")
                                        .resourceId(shortlist.getId())
                                        .guestName(recipient.getName())
                                        .guestEmail(recipient.getEmail())
                                        .write(true)
                                        .redirectUrl(appBaseUrl + "/shares/shortlist/" + shortlist.getId())
                                        .expiryHours(expiryHours)
                                        .build()).getBody();

                        String shareLink = guestLoginUri + "?guest_code=" + response.getGuestCode() + "&resource_id="
                                        + shortlist.getId();
                        shareLinks.put(recipient.getEmail(), shareLink);

                        log.info("Created shared shortlist for recipient: {}, shortlistId: {}, with {} submissions",
                                        recipient.getEmail(), shortlist.getId(), submissions.size());
                }

                return shareLinks;
        }
}