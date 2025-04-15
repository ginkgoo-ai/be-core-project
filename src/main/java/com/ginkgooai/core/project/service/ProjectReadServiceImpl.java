package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import com.ginkgooai.core.project.dto.response.*;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectReadServiceImpl implements ProjectReadService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectRoleRepository projectRoleRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private StorageClient storageClient;

    @Override
    public Optional<ProjectResponse> findById(String workspaceId, String id) {
        return projectRepository.findByIdAndWorkspaceId(id, workspaceId)
                .map(ProjectResponse::from);
    }

    @Override
    public List<ProjectResponse> findAll() {
        return projectRepository.findByWorkspaceId(ContextUtils.getWorkspaceId()).stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
	public Page<ProjectListResponse> findProjectList(String name, ProjectStatus status, Pageable pageable) {
		return projectRepository.findProjectList(ContextUtils.getWorkspaceId(), name, status, pageable);
    }

    @Override
    public List<ProjectBasicResponse> findAllBasicInfo() {
        String workspaceId = ContextUtils.getWorkspaceId();
        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(ProjectBasicResponse::fromProject)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> findByOwnerId(String ownerId) {
        return projectRepository.findByCreatedBy(ownerId).stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProjectRole> findRoleById(String roleId) {
        return projectRoleRepository.findById(roleId);
    }

    @Override
    public ProjectRoleStatisticsResponse getRoleStatistics(String roleId) {
        return applicationRepository.getRoleStatistics(roleId);
    }

    @Override
    public Page<ProjectRoleStatisticsResponse> getProjectRolesStatistics(String projectId, String name,
            Pageable pageable) {
        Page<ProjectRoleStatisticsResponse> statisticsPage = applicationRepository.getProjectRolesStatistics(projectId,
                name, pageable);

        List<String> allSideFileIds = statisticsPage.getContent().stream()
                .filter(stat -> stat.getSideFileIds() != null)
                .flatMap(role -> Arrays.stream(role.getSideFileIds()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, CloudFileResponse> sideFilesMap = Collections.emptyMap();
        if (!allSideFileIds.isEmpty()) {
            try {
                ResponseEntity<List<CloudFileResponse>> response = storageClient.getFileDetails(allSideFileIds);
                if (response.getBody() != null) {
                    sideFilesMap = response.getBody().stream()
                            .collect(Collectors.toMap(CloudFileResponse::getId, Function.identity()));
                }
            } catch (Exception e) {
                log.error("Error fetching side files: {}", e.getMessage());
            }
        }

        Map<String, CloudFileResponse> finalSideFilesMap = sideFilesMap;
        statisticsPage.getContent().forEach(stat -> {
            if (stat.getSideFileIds() != null) {
                List<CloudFileResponse> sideFiles = Arrays.stream(stat.getSideFileIds())
                        .map(side -> finalSideFilesMap.get(side))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                stat.setSides(sideFiles);
            }
        });

        return statisticsPage;
    }

    @Override
    public List<ProjectRole> findRolesByProjectId(String projectId) {
        return projectRoleRepository.findByProjectId(projectId);
    }

    @Override
    public Page<ProjectRole> findRolesByProjectIdPaginated(String projectId, Pageable pageable) {
        return projectRoleRepository.findByProjectId(projectId, pageable);
    }


    @Override
    public List<RoleBasicResponse> findAllRolesBasicInfo(String projectId) {
        return projectRoleRepository.findByProjectId(projectId).stream()
                .map(RoleBasicResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectStatisticsResponse getProjectsStatistics() {
        String workspaceId = ContextUtils.getWorkspaceId();

        long activeProjects = projectRepository.countByWorkspaceIdAndStatusIn(
            workspaceId,
            List.of(ProjectStatus.DRAFTING, ProjectStatus.IN_PROGRESS)
        );

        long rolesToFill = projectRoleRepository.countByWorkspaceIdAndStatusNot(
            workspaceId,
            RoleStatus.CAST
        );

        long pendingReviews = applicationRepository.countByWorkspaceIdAndStatus(
            workspaceId,
            ApplicationStatus.SUBMITTED
        );

        long unviewedVideos = submissionRepository.countUnviewedSubmissions(
            workspaceId
        );

        return new ProjectStatisticsResponse(
            activeProjects,
            rolesToFill,
            pendingReviews,
            unviewedVideos
        );
    }


}