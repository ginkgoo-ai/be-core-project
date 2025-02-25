package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.bean.ActivityType;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.*;
import com.ginkgooai.core.project.dto.request.*;
import com.ginkgooai.core.project.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProjectWriteServiceImpl implements ProjectWriteService {

    private static final String LOCK_PREFIX = "lock:project:"; // Prefix for Redisson lock keys
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectRoleRepository projectRoleRepository;
    @Autowired
    private ProjectNdaRepository projectNdaRepository;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private ProjectActivityRepository projectActivityRepository;
    @Autowired
    private RedissonClient redissonClient; // Inject Redisson client

    @Override
    @Transactional
    public Project createProject(ProjectRequest request) {
        log.debug("Creating new project with request: {}", request);

        validateProjectRequest(request);

        validateOwner(request.getOwnerId());
        
        Project project = Project.createFromRequest(request);
        Project savedProject = projectRepository.save(project);

        // Set optional status if provided (override default DRAFTING if necessary)
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        // Initialize roles and bind to project
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleRequest -> {
                ProjectRole role = new ProjectRole();
                role.setName(roleRequest.getName());
                role.setCharacterDescription(roleRequest.getCharacterDescription());
                role.setSelfTapeInstructions(roleRequest.getSelfTapeInstructions());
                role.setAuditionNotes(roleRequest.getAuditionNotes());
                role.setAgeRange(roleRequest.getAgeRange());
                role.setGender(roleRequest.getGender());
                role.setIsActive(true);
                project.addRole(role);
            });
        }

        if (request.getNdaIds() != null && !request.getNdaIds().isEmpty()) {
            request.getNdaIds().forEach(ndaId -> {
                ProjectNda nda = new ProjectNda(); // Placeholder, replace with actual entity from DB
                nda.setId(ndaId);
                nda.setProject(project);
                projectNdaRepository.save(nda);
                project.addNda(nda);
            });
        }

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            request.getMemberIds().forEach(memberId -> {
                ProjectMember member = new ProjectMember(); // Placeholder, replace with actual entity from DB
                member.setId(memberId);
                member.setProject(project);
                projectMemberRepository.save(member);
                project.addMember(member);
            });
        }

        // Log creation activity (handled by Project itself)
        ProjectActivity activity = new ProjectActivity();
        activity.setActivityType(ActivityType.PROJECT_CREATED);
        activity.setStatus(ActivityStatus.SUBMITTED);
        activity.setDescription("Project " + project.getName() + " created");
        activity.setProject(savedProject);
        activity.setCreatedAt(LocalDateTime.now());
        projectActivityRepository.save(activity);

        return savedProject;
    }

    private void validateProjectRequest(ProjectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Project request cannot be null");
        }
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (StringUtils.isBlank(request.getOwnerId())) {
            throw new IllegalArgumentException("Owner ID cannot be empty");
        }
    }

    private void validateOwner(String ownerId) {
//        if (!userService.isUserActive(ownerId)) {
//            throw new IllegalArgumentException("Invalid owner ID or owner is not active");
//        }
    }


    @Override
    @Transactional
    public Project updateProject(String id, ProjectRequest request) {
        String lockKey = LOCK_PREFIX + id; // Lock based on project ID
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                Project project = projectRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Project not found"));

                project.updateDetails(request.getName(), request.getDescription(), request.getPlotLine(), request.getStatus());

                // Update relationships (handled by Project's methods)
                updateRelationships(project, request);

                return projectRepository.save(project);
            } else {
                throw new RuntimeException("Failed to acquire lock for updating project with ID: " + id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted for updating project with ID: " + id, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void updateRelationships(Project project, ProjectRequest request) {
        // Clear and re-add roles
        project.getRoles().clear();
//        request.getRoleIds().forEach(roleId -> {
//            ProjectRole role = projectRoleRepository.findById(roleId)
//                    .orElseThrow(() -> new RuntimeException("Role not found"));
//            project.addRole(role);
//        });

        // Clear and re-add NDAs
//        project.getNdas().clear();
//        request.getNdaIds().forEach(ndaId -> {
//            ProjectNda nda = projectNdaRepository.findById(ndaId)
//                    .orElseThrow(() -> new RuntimeException("NDA not found"));
//            project.addNda(nda);
//        });

        // Clear and re-add members
        project.getMembers().clear();
        request.getMemberIds().forEach(memberId -> {
            ProjectMember member = projectMemberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            project.addMember(member);
        });

        // Clear and re-add activities
        project.getActivities().clear();
        request.getActivityIds().forEach(activityId -> {
            ProjectActivity activity = projectActivityRepository.findById(activityId)
                    .orElseThrow(() -> new RuntimeException("Activity not found"));
            project.addActivity(activity);
        });
    }

    @Override
    @Transactional
    public void deleteProject(String id) {
        String lockKey = LOCK_PREFIX + id; // Lock based on project ID
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                Project project = projectRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Project not found"));
                projectRepository.delete(project);
            } else {
                throw new RuntimeException("Failed to acquire lock for deleting project with ID: " + id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted for deleting project with ID: " + id, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public ProjectRole createRole(String projectId, ProjectRoleRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "projectId", projectId));

        ProjectRole role = new ProjectRole();
        role.setName(request.getName());
        role.setCharacterDescription(request.getCharacterDescription());
        role.setSelfTapeInstructions(request.getSelfTapeInstructions());
        role.setAuditionNotes(request.getAuditionNotes());
        role.setAgeRange(request.getAgeRange());
        role.setGender(request.getGender());
        role.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        role.setProject(project);

        ProjectRole savedRole = projectRoleRepository.save(role);
        project.addRole(savedRole); // Update the project aggregate
        projectRepository.save(project); // Save to persist the relationship

        // Log activity
        ProjectActivity activity = new ProjectActivity();
        activity.setActivityType(ActivityType.ROLE_SUBMISSION);
        activity.setStatus(ActivityStatus.SUBMITTED);
        activity.setDescription("Role " + role.getName() + " added to project");
        activity.setProject(project);
        activity.setCreatedAt(LocalDateTime.now());
        project.addActivity(activity);
        projectRepository.save(project); // Save to persist the activity

        return savedRole;
    }

    @Override
    @Transactional
    public ProjectNda createNda(String projectId, ProjectNdaRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "projectId", projectId));

        ProjectNda nda = new ProjectNda();
        nda.setRequiresNda(request.getRequiresNda() != null ? request.getRequiresNda() : false);
        nda.setApplyToAll(request.getApplyToAll() != null ? request.getApplyToAll() : false);
        nda.setVersion(request.getVersion());
        nda.setFullName(request.getFullName());
        nda.setTitle(request.getTitle());
        nda.setCompany(request.getCompany());
        nda.setSignatureUrl(request.getSignatureUrl());
        nda.setProject(project);

        ProjectNda savedNda = projectNdaRepository.save(nda);
        project.addNda(savedNda); // Update the project aggregate
        projectRepository.save(project); // Save to persist the relationship

        // Log activity
        ProjectActivity activity = new ProjectActivity();
        activity.setActivityType(ActivityType.NDA_SIGNED);
        activity.setStatus(ActivityStatus.SUBMITTED);
        activity.setDescription("NDA created for project");
        activity.setProject(project);
        activity.setCreatedAt(LocalDateTime.now());
        project.addActivity(activity);
        projectRepository.save(project); // Save to persist the activity

        return savedNda;
    }

    @Override
    @Transactional
    public ProjectMember addMember(String projectId, ProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "projectId", projectId));
        
        ProjectMember member = new ProjectMember();
        member.setUserId(request.getUserId());
        member.setStatus(request.getStatus() != null ? request.getStatus() : MemberStatus.ACTIVE);
        member.setProject(project);

        // Optionally set the role if provided
        if (request.getRoleId() != null) {
            ProjectRole role = projectRoleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleId()));
        }

        ProjectMember savedMember = projectMemberRepository.save(member);
        project.addMember(savedMember); // Update the project aggregate
        projectRepository.save(project); // Save to persist the relationship

        // Log activity
        ProjectActivity activity = new ProjectActivity();
        activity.setActivityType(ActivityType.MEMBER_ADDED);
        activity.setStatus(ActivityStatus.SUBMITTED);
        activity.setDescription("Member " + member.getUserId() + " added to project");
        activity.setProject(project);
        activity.setCreatedAt(LocalDateTime.now());
        project.addActivity(activity);
        projectRepository.save(project); // Save to persist the activity

        return savedMember;
    }

    @Override
    @Transactional
    public ProjectActivity logActivity(String projectId, ProjectActivityRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "projectId", projectId));

        ProjectActivity activity = new ProjectActivity();
        activity.setActivityType(request.getActivityType());
        activity.setStatus(request.getStatus() != null ? request.getStatus() : ActivityStatus.SUBMITTED);
        activity.setDescription(request.getDescription());
        activity.setProject(project);
        activity.setCreatedAt(LocalDateTime.now());

        ProjectActivity savedActivity = projectActivityRepository.save(activity);
        project.addActivity(savedActivity); // Update the project aggregate
        projectRepository.save(project); // Save to persist the relationship

        return savedActivity;
    }
}