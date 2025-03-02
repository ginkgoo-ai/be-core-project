package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.domain.project.*;
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
    private RedissonClient redissonClient; // Inject Redisson client

    @Override
    @Transactional
    public Project createProject(ProjectRequest request, String workspaceId) {
        log.debug("Creating new project with request: {}", request);

        validateProjectRequest(request);

        validateOwner(request.getOwnerId());

        Project project = new Project(request, workspaceId);
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
                role.setIsActive(true);
                role.setProject(savedProject);
//                project.addRole(role);
                projectRoleRepository.save(role);
            });
        }

//        if (request.getNdaIds() != null && !request.getNdaIds().isEmpty()) {
//            request.getNdaIds().forEach(ndaId -> {
//                ProjectNda nda = new ProjectNda(); // Placeholder, replace with actual entity from DB
//                nda.setProject(savedProject);
//                projectNdaRepository.save(nda);
//                project.addNda(nda);
//            });
//        }

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            request.getMemberIds().forEach(memberId -> {
                ProjectMember member = new ProjectMember(); // Placeholder, replace with actual entity from DB
                member.setUserId(memberId);
                member.setProject(savedProject);
                projectMemberRepository.save(member);
            });
        }

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

    @Override
    @Transactional
    public Project updateProjectStatus(String id, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        project.setStatus(status);
        project.setLastActivityAt(LocalDateTime.now());

        return projectRepository.save(project);
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
        role.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        role.setProject(project);

        ProjectRole savedRole = projectRoleRepository.save(role);
        project.addRole(savedRole); // Update the project aggregate
        projectRepository.save(project); // Save to persist the relationship

        return savedRole;
    }

    @Override
    @Transactional
    public ProjectRole updateRole(String roleId, ProjectRoleRequest request) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        role.setName(request.getName());
        role.setCharacterDescription(request.getCharacterDescription());
        role.setSelfTapeInstructions(request.getSelfTapeInstructions());
        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }

        ProjectRole savedRole = projectRoleRepository.save(role);

        return savedRole;
    }

    @Override
    public ProjectRole patchRoleDetails(String roleId, ProjectRolePatchRequest request) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        // Only update fields that are provided in the request
        if (request.getCharacterDescription() != null) {
            role.setCharacterDescription(request.getCharacterDescription());
        }
        if (request.getSelfTapeInstructions() != null) {
            role.setSelfTapeInstructions(request.getSelfTapeInstructions());
        }
        if (request.getSides() != null) {
            role.setSides(request.getSides());
        }
        return projectRoleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));

        Project project = role.getProject();
        project.removeRole(roleId);

        projectRepository.save(project);
        projectRoleRepository.delete(role);
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

        return savedNda;
    }

    @Override
    @Transactional
    public ProjectMember addMember(String projectId, ProjectMemberRequest request) {
       return new ProjectMember(); 
    }
}