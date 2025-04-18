package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.constant.ContextsConstant;
import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.enums.Role;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.common.utils.UrlUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.PatchUserRequest;
import com.ginkgooai.core.project.client.identity.dto.ShareCodeRequest;
import com.ginkgooai.core.project.client.identity.dto.ShareCodeResponse;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.domain.application.*;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
import com.ginkgooai.core.project.dto.response.ShortlistShareResponse;
import com.ginkgooai.core.project.repository.ShortlistItemRepository;
import com.ginkgooai.core.project.repository.ShortlistRepository;
import com.ginkgooai.core.project.repository.ShortlistShareRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.repository.specification.ShortlistItemSpecification;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShortlistService {

	private final ShortlistRepository shortlistRepository;

	private final ShortlistItemRepository shortlistItemRepository;

	private final SubmissionRepository submissionRepository;

	private final ShortlistShareRepository shortlistShareRepository;

	private final IdentityClient identityClient;

	private final String appBaseUrl;

	private final ActivityLoggerService activityLogger;


	public ShortlistService(ShortlistRepository shortlistRepository, ShortlistItemRepository shortlistItemRepository,
			SubmissionRepository submissionRepository, ShortlistShareRepository shortlistShareRepository,
			IdentityClient identityClient, ActivityLoggerService activityLogger,
			@Value("${spring.security.oauth2.guest_login_uri}") String guestLoginUri,
			@Value("${app.base-uri}") String appBaseUrl) {
		this.shortlistRepository = shortlistRepository;
		this.shortlistItemRepository = shortlistItemRepository;
		this.submissionRepository = submissionRepository;
		this.shortlistShareRepository = shortlistShareRepository;
		this.identityClient = identityClient;
		this.appBaseUrl = appBaseUrl;
		this.activityLogger = activityLogger;
	}

	@Transactional
	public void addShortlistItem(String userId, String submissionId, String notes) {
		Submission submission = submissionRepository.findById(submissionId)
			.orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

		Application application = submission.getApplication();

		// Try to find existing shortlist
		Shortlist shortlist = shortlistRepository
			.findByWorkspaceIdAndProjectIdAndOwnerId(application.getWorkspaceId(), application.getProject().getId(),
					userId)
			.orElseGet(() -> {
				// If shortlist doesn't exist, create a new one
				Shortlist shortlistNew = Shortlist.builder()
					.workspaceId(ContextUtils.get().getWorkspaceId())
					.projectId(application.getProject().getId())
					.ownerId(userId)
					.ownerType(OwnerType.INTERNAL)
					.name("Shortlist")
					.build();
				return shortlistRepository.save(shortlistNew);
			});

		// Get the maximum order value
		Integer maxOrder = CollectionUtils.isEmpty(shortlist.getItems()) ? 0
				: shortlist.getItems().stream().map(ShortlistItem::getSortOrder).max(Integer::compareTo).orElse(0);

		ShortlistItem shortlistItem = shortlistItemRepository
			.findByApplicationIdAndShortlistId(application.getId(), shortlist.getId())
			.orElseGet(() -> {
				// Create new shortlist item if not exists
				ShortlistItem newItem = ShortlistItem.builder()
					.application(application)
					.shortlist(shortlist)
					.submissions(new ArrayList<>())
					.sortOrder(maxOrder + 1)
					.submissions(new ArrayList<>())
					.build();
				return shortlistItemRepository.save(newItem);
			});
		application.setStatus(ApplicationStatus.SHORTLISTED);
		application.getRole().setStatus(RoleStatus.SHORTLISTED);

		// Add submission to existing shortlist item if not already present
		if (!shortlistItem.getSubmissions().contains(submission)) {
			shortlistItem.getSubmissions().add(submission);
			shortlistItemRepository.save(shortlistItem);
			log.debug("Added submission {} to shortlist item {}", submissionId, shortlistItem.getId());
		}

		// Log activity
		activityLogger.log(
			application.getWorkspaceId(),
			application.getProject().getId(),
			application.getId(),
			ActivityType.SUBMISSION_ADDED_TO_SHORTLIST,
			Map.of(
				"talentName", String.join(" ", application.getTalent().getFirstName(), application.getTalent().getLastName()),
						"roleName", application.getRole().getName(), "project", application.getProject()
							.getName()
			),
			Map.of(
				submission.getVideoName(), submission.getVideoUrl()
			),
			ContextUtils.getUserId()
		);
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

		// ShortlistItem shortlistItem = shortlistItems.get(0);

		shortlistItems.forEach(shortlistItem -> {
			shortlistItem.getSubmissions().remove(submission);
			submission.getShortlistItems().remove(shortlistItem);

			if (shortlistItem.getSubmissions().isEmpty()) {
				shortlistItemRepository.delete(shortlistItem);
				log.debug("Deleted empty shortlist item: {}", shortlistItem.getId());
			}
			else {
				shortlistItemRepository.save(shortlistItem);
				log.debug("Removed submission {} from shortlist item {}", submissionId, shortlistItem.getId());
			}
		});

		submissionRepository.save(submission);
	}

	private Shortlist findShortlistById(String shortlistId) {
		return shortlistRepository.findById(shortlistId)
			.orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));
	}

	@Transactional(readOnly = true)
	public Page<ShortlistItemResponse> listShortlistItems(String projectId, String keyword, String roleId, String talentId,
	                                                      LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
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
				ShortlistItemSpecification.findAllWithFilters(
					shortlist.getId(),
					keyword,
					roleId,
					startDateTime,
					endDateTime,
					talentId),
				pageable)
			.map(t -> ShortlistItemResponse.from(t, userId));
	}

	@Transactional(readOnly = true)
	public Page<ShortlistItemResponse> listShortlistItemsByShortlistId(String shortlistId, String keyword, String roleId, String talentId,
	                                                                   LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {

		return shortlistItemRepository.findAll(
			ShortlistItemSpecification.findAllWithFilters(
				shortlistId,
				keyword,
				roleId,
				startDateTime,
				endDateTime,
				talentId
			),
			pageable
		).map(t -> ShortlistItemResponse.from(t, ContextUtils.getUserId()));
	}

	private Specification<ShortlistItem> buildShortlistItemSpecification(String shortlistId, String keyword) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("shortlist").get("id"), shortlistId));

			if (keyword != null && !keyword.trim().isEmpty()) {
				String likePattern = "%" + keyword.toLowerCase() + "%";

				List<Predicate> keywordPredicates = new ArrayList<>();

				Join<ShortlistItem, Application> applicationJoin = root.join("application", JoinType.LEFT);

				Join<Application, ProjectRole> roleJoin = applicationJoin.join("role", JoinType.LEFT);
				keywordPredicates.add(cb.like(cb.lower(roleJoin.get("name")), likePattern));

				Join<Application, Talent> talentJoin = applicationJoin.join("talent", JoinType.LEFT);
				keywordPredicates.add(cb.like(cb.lower(talentJoin.get("firstName")), likePattern));
				keywordPredicates.add(cb.like(cb.lower(talentJoin.get("lastName")), likePattern));
				keywordPredicates.add(cb.like(cb.lower(talentJoin.get("email")), likePattern));

				Join<ShortlistItem, Submission> submissionsJoin = root.join("submissions", JoinType.LEFT);
				keywordPredicates.add(cb.like(cb.lower(submissionsJoin.get("videoName")), likePattern));

				predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
			}

			query.distinct(true);

			query.orderBy(cb.asc(root.get("sortOrder")));

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	@Transactional
	public Map<String, String> shareShortlist(ShareShortlistRequest request) {
		Map<String, String> shareLinks = new HashMap<>();
		String workspaceId = ContextUtils.get().getWorkspaceId();
		String userId = ContextUtils.get(ContextsConstant.USER_ID, String.class, null);

		// First get the user's shortlist for this project
		Shortlist shortlist = shortlistRepository
			.findByWorkspaceIdAndProjectIdAndOwnerId(workspaceId, request.getProjectId(), userId)
			.orElse(null);
		if (shortlist == null) {
			throw new ResourceNotFoundException("Shortlist", "projectId-userId",
					String.join("-", userId, request.getProjectId()));
		}

		String shortlistId = shortlist.getId();
		for (ShareShortlistRequest.Recipient recipient : request.getRecipients()) {
			Integer expiryHours = request.getExpiresInDays() != null ? request.getExpiresInDays() * 24 : 7 * 24;

			Optional<ShortlistShare> existingShare = shortlistShareRepository
				.findActiveShareByShortlistIdAndEmail(shortlistId, recipient.getEmail());

			ShareCodeResponse response;
			if (existingShare.isPresent()) {
				shareLinks.put(recipient.getEmail(), existingShare.get().getShareLink());
			}
			else {
				response = identityClient
					.generateShareCode(ShareCodeRequest.builder()
						.workspaceId(workspaceId)
						.resource("shortlist")
						.resourceId(shortlistId)
						.guestName(String.join(" ", recipient.getFirstName(), recipient.getLastName()))
						.guestEmail(recipient.getEmail())
						.roles(List.of(Role.ROLE_PRODUCER))
						.write(true)
						.expiryHours(expiryHours)
						.build())
					.getBody();

				String baseUrl = request.getRedirectUrl().replace("{id}", shortlistId);
				String shareLink = UrlUtils.appendQueryParam(baseUrl, "share_code", response.getShareCode());
				shareLinks.put(recipient.getEmail(), shareLink);

				LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiryHours);
				ShortlistShare share = ShortlistShare.builder()
					.shortlist(shortlist)
					.shareLink(shareLink)
					.recipientId(response.getUserId())
					.recipientEmail(recipient.getEmail())
					.recipientName(String.join(" ", recipient.getFirstName(), recipient.getLastName()))
					.shareCode(response.getShareCode())
					.expiresAt(expiresAt)
					.active(true)
					.build();
				shortlistShareRepository.save(share);

				UserInfoResponse userInfoResponse = identityClient.getUserById(response.getUserId()).getBody();
				if ((!Objects.equals(userInfoResponse.getFirstName(), recipient.getFirstName())
						|| !Objects.equals(userInfoResponse.getLastName(), recipient.getLastName()))
						&& !userInfoResponse.getRoles().contains(Role.ROLE_USER.name())) {
					identityClient.patchUserInfo(userInfoResponse.getId(),
							PatchUserRequest.builder()
								.firstName(recipient.getFirstName())
								.lastName(recipient.getLastName())
								.build());
				}
			}

			log.info("Created shared shortlist for recipient: {}, shortlistId: {}", recipient.getEmail(), shortlistId);
		}

		return shareLinks;
	}

	@Transactional(readOnly = true)
	public List<ShortlistShareResponse> getShortlistShares(String projectId) {
		String workspaceId = ContextUtils.get().getWorkspaceId();
		String userId = ContextUtils.get(ContextsConstant.USER_ID, String.class, null);

		Shortlist shortlist = shortlistRepository
			.findByWorkspaceIdAndProjectIdAndOwnerId(workspaceId, projectId, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Shortlist", "projectId-userId",
					String.join("-", projectId, userId)));

		List<ShortlistShare> shares = shortlistShareRepository.findByShortlistIdAndActiveTrue(shortlist.getId());

		return shares.stream().map(share -> ShortlistShareResponse.from(share)).collect(Collectors.toList());
	}

	@Transactional
	public void revokeShortlistShare(String shareId) {
		ShortlistShare share = shortlistShareRepository.findById(shareId)
			.orElseThrow(() -> new ResourceNotFoundException("ShortlistShare", "id", shareId));

		share.setActive(false);
		shortlistShareRepository.save(share);
		try {
			identityClient.revokeShareCode(share.getShareCode());
		}
		catch (Exception e) {
			log.error("Failed to revoke share code from identity service: {}", e.getMessage());
		}

		log.info("Revoked shortlist share: {}, recipient: {}", shareId, share.getRecipientEmail());
	}

	@Transactional(readOnly = true)
	public ShortlistItemResponse getShortlistItemById(String shortlistItemId) {
		ShortlistItem shortlistItem = shortlistItemRepository.findById(shortlistItemId)
			.orElseThrow(() -> new ResourceNotFoundException("ShortlistItem", "id", shortlistItemId));

		List<String> userIds = new ArrayList<>();
		shortlistItem.getSubmissions()
			.forEach(submission -> submission.getComments().forEach(comment -> userIds.add(comment.getCreatedBy())));

		final List<UserInfoResponse> commentUsers = getUserInfoByIds(userIds);
		Shortlist shortlist = shortlistItem.getShortlist();
		return ShortlistItemResponse.from(shortlistItem, commentUsers, shortlist.getOwnerId());
	}

	private List<UserInfoResponse> getUserInfoByIds(List<String> userIds) {
		List<String> distinctUserIds = userIds.stream()
			.filter(id -> id != null && !id.isEmpty())
			.distinct()
			.collect(Collectors.toList());

		List<UserInfoResponse> users = new ArrayList<>();
		if (!distinctUserIds.isEmpty()) {
			try {
				users = identityClient.getUsersByIds(distinctUserIds).getBody();
				if (users == null) {
					users = new ArrayList<>();
					log.warn("Failed to get user information from identity service");
				}
			}
			catch (Exception e) {
				log.error("Error fetching user information: {}", e.getMessage());
			}
		}

		return users;
	}
}
