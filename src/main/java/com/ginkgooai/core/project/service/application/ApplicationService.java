package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ActivityLogger;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationComment;
import com.ginkgooai.core.project.domain.application.ApplicationNote;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.dto.request.ApplicationRequest;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStateMachine stateMachine;
//    private final VideoService videoService;
    private final ActivityLogger activityLogger;
//    private final EmailService emailService;

    @Transactional
    public Application createApplication(ApplicationRequest request, String userId) {
        Application application = new Application();
        application.setStatus(ApplicationStatus.ADDED);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application sendInvitation(String id, String userId) {
        Application application = getApplicationById(id);
        
        // Validate state transition
        stateMachine.validateTransition(application.getStatus(), ApplicationStatus.REQUESTED);
        
        // Send invitation email
//        emailService.sendApplicationInvitation(application);
        
        // Update status
        application.setStatus(ApplicationStatus.REQUESTED);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application declineInvitation(String id, String reason) {
        Application application = getApplicationById(id);
        
        stateMachine.validateTransition(application.getStatus(), ApplicationStatus.DECLINED);
        
        application.setStatus(ApplicationStatus.DECLINED);
//        application.getContext().put("declineReason", reason);
        
        return applicationRepository.save(application);
    }

    @Transactional
    public Application uploadVideo(String id, MultipartFile video, String userId) {
        Application application = getApplicationById(id);
        
        stateMachine.validateTransition(application.getStatus(), ApplicationStatus.SUBMITTED);
        
        // Process and store video
//        VideoInfo videoInfo = videoService.processAndStoreVideo(video);
//        application.setVideoUrl(videoInfo.getUrl());
//        application.setVideoThumbnailUrl(videoInfo.getThumbnailUrl());
//        application.setVideoDuration(videoInfo.getDuration());
//        application.setVideoResolution(videoInfo.getResolution());
        
        application.setStatus(ApplicationStatus.SUBMITTED);
        
        return applicationRepository.save(application);
    }

//    @Transactional
//    public Application reviewApplication(String id, ReviewRequest request, String reviewerId) {
//        Application application = getApplicationById(id);
//        
//        stateMachine.validateTransition(application.getStatus(), ApplicationStatus.REVIEWED);
//        
//        application.setStatus(ApplicationStatus.REVIEWED);
//        application.setReviewedBy(reviewerId);
//        application.setReviewedAt(LocalDateTime.now());
//        application.setReviewNotes(request.getNotes());
//        
//        return applicationRepository.save(application);
//    }
//
//    @Transactional
//    public Application requestRetape(String id, String reason, String userId) {
//        Application application = getApplicationById(id);
//        
//        stateMachine.validateTransition(application.getStatus(), ApplicationStatus.RETAPE);
//        
//        application.setStatus(ApplicationStatus.RETAPE);
//        application.getContext().put("retapeReason", reason);
//        
//        // Send retape request email
//        emailService.sendRetapeRequest(application, reason);
//        
//        return applicationRepository.save(application);
//    }

    @Transactional
    public Application shortlistApplication(String id, String notes, String userId) {
        Application application = getApplicationById(id);
        
        stateMachine.validateTransition(application.getStatus(), ApplicationStatus.SHORTLISTED);
        
        application.setStatus(ApplicationStatus.SHORTLISTED);
        application.setShortlisted(true);
        if (notes != null) {
            application.setReviewNotes(notes);
        }
        
        return applicationRepository.save(application);
    }

    public Application getApplicationById(String id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(String projectId, String roleId, String talentId, Pageable pageable) {
        String workspaceId = "";

        return applicationRepository.findAll(buildSpecification(workspaceId, projectId, roleId, talentId), pageable)
                .map(ApplicationResponse::from);
    }

    private Specification<Application> buildSpecification(String workspaceId, String projectId, String roleId, String talentId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Workspace filter (required)
            predicates.add(criteriaBuilder.equal(root.get("workspaceId"), workspaceId));

            // Project filter
            if (StringUtils.hasText(projectId)) {
                predicates.add(criteriaBuilder.equal(root.get("projectId"), projectId));
            }

            // Role filter
            if (StringUtils.hasText(roleId)) {
                predicates.add(criteriaBuilder.equal(root.get("roleId"), roleId));
            }

            // Talent filter
            if (StringUtils.hasText(talentId)) {
                predicates.add(criteriaBuilder.equal(root.get("talent").get("id"), talentId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Application findApplicationById(String id) {
        String workspaceId = "";

        return applicationRepository.findOne(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("id"), id),
                        cb.equal(root.get("workspaceId"), workspaceId)
                )
        ).orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }


    @Transactional
    public ApplicationResponse toggleShortlist(String id, boolean shortlisted) {
        Application application = findApplicationById(id);
        application.setShortlisted(shortlisted);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse addComment(String id, String userId, String content) {
        Application application = findApplicationById(id);
//        String userId = getCurrentUserId();

        ApplicationComment comment = ApplicationComment.builder()
                .application(application)
                .content(content)
                .createdBy(userId)
                .build();

        application.getComments().add(comment);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse addNote(String id, String userId, String content) {
        Application application = findApplicationById(id);

        ApplicationNote note = ApplicationNote.builder()
                .application(application)
                .content(content)
                .createdBy(userId)
                .build();

        application.getNotes().add(note);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

}