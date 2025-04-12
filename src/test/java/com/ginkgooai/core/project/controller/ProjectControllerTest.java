package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import com.ginkgooai.core.project.dto.request.ProjectCreateRequest;
import com.ginkgooai.core.project.dto.request.ProjectResponse;
import com.ginkgooai.core.project.dto.request.ProjectUpdateRequest;
import com.ginkgooai.core.project.dto.request.ProjectUpdateStatusRequest;
import com.ginkgooai.core.project.dto.response.ProjectBasicResponse;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import com.ginkgooai.core.project.service.ProjectReadService;
import com.ginkgooai.core.project.service.ProjectWriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectReadService projectReadService;

    @Mock
    private ProjectWriteService projectWriteService;

    @Mock
    private ActivityLoggerService activityLogger;

    @InjectMocks
    private ProjectController projectController;

    private ProjectCreateRequest createRequest;
    private ProjectUpdateRequest updateRequest;
    private ProjectUpdateStatusRequest statusRequest;
    private Project project;
    private ProjectResponse projectResponse;
    private List<ProjectBasicResponse> basicResponses;
    private MockedStatic<ContextUtils> contextUtilsMockedStatic;
    private String workspaceId = "workspace-1";
    private String userId = "user-1";
    private String projectId = "project-1";

    @BeforeEach
    void setUp() {
        contextUtilsMockedStatic = mockStatic(ContextUtils.class);
        contextUtilsMockedStatic.when(ContextUtils::getWorkspaceId).thenReturn(workspaceId);
        contextUtilsMockedStatic.when(ContextUtils::getUserId).thenReturn(userId);

        createRequest = new ProjectCreateRequest();
        createRequest.setName("测试项目");
        createRequest.setDescription("这是一个测试项目");
        createRequest.setPlotLine("测试剧情");

        updateRequest = new ProjectUpdateRequest();
        updateRequest.setName("更新的项目");
        updateRequest.setDescription("这是更新后的项目");
        updateRequest.setPlotLine("更新的剧情");

        statusRequest = new ProjectUpdateStatusRequest();
        statusRequest.setStatus(ProjectStatus.IN_PROGRESS);

        project = Project.builder()
                .id(projectId)
                .name("测试项目")
                .description("这是一个测试项目")
                .plotLine("测试剧情")
                .status(ProjectStatus.DRAFTING)
                .workspaceId(workspaceId)
                .build();

        projectResponse = new ProjectResponse();
        projectResponse.setId(projectId);
        projectResponse.setName("测试项目");
        projectResponse.setDescription("这是一个测试项目");
        projectResponse.setPlotLine("测试剧情");
        projectResponse.setStatus(ProjectStatus.DRAFTING);

        // 基本响应列表
        basicResponses = Arrays.asList(
                new ProjectBasicResponse(projectId, "测试项目"),
                new ProjectBasicResponse("project-2", "另一个项目"));
    }

    @AfterEach
    void tearDown() {
        if (contextUtilsMockedStatic != null) {
            contextUtilsMockedStatic.close();
        }
    }

    @Test
    void createProject_Success() {
        when(projectWriteService.createProject(any(ProjectCreateRequest.class)))
                .thenReturn(ProjectResponse.from(project));
        
        ResponseEntity<ProjectResponse> response = projectController.createProject(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(projectWriteService).createProject(eq(createRequest));
    }

    @Test
    void getProjectById_Success() {
        when(projectReadService.findById(workspaceId, projectId)).thenReturn(Optional.of(projectResponse));

        ResponseEntity<ProjectResponse> response = projectController.getProjectById(projectId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(projectId, response.getBody().getId());
    }

    @Test
    void getProjectById_NotFound() {
        when(projectReadService.findById(workspaceId, projectId)).thenReturn(Optional.empty());

        ResponseEntity<ProjectResponse> response = projectController.getProjectById(projectId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProjects_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<ProjectResponse> projectList = Arrays.asList(projectResponse);
        Page<ProjectResponse> projectPage = new PageImpl<>(projectList, pageable, 1);

        when(projectReadService.findProjects(eq(workspaceId), anyString(), any(ProjectStatus.class),
                any(Pageable.class)))
                .thenReturn(projectPage);

        ResponseEntity<Page<ProjectResponse>> response = projectController.getProjects(
                0, 10, "DESC", "updatedAt", "测试", ProjectStatus.DRAFTING);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getProjects_BadRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            projectController.getProjects(0, 10, "INVALID", "updatedAt", "测试", ProjectStatus.DRAFTING);
        });
    }

    @Test
    void getAllProjectsBasicInfo_Success() {
        when(projectReadService.findAllBasicInfo()).thenReturn(basicResponses);

        ResponseEntity<List<ProjectBasicResponse>> response = projectController.getAllProjectsBasicInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void updateProject_Success() {
        Project updatedProject = Project.builder()
                .id(projectId)
                .name("更新的项目")
                .description("这是更新后的项目")
                .plotLine("更新的剧情")
                .status(ProjectStatus.DRAFTING)
                .workspaceId(workspaceId)
                .build();

        when(projectWriteService.updateProject(eq(projectId), any(ProjectUpdateRequest.class), eq(workspaceId)))
                .thenReturn(updatedProject);

        ResponseEntity<ProjectResponse> response = projectController.updateProject(projectId, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateProject_NotFound() {
        when(projectWriteService.updateProject(eq(projectId), any(ProjectUpdateRequest.class), eq(workspaceId)))
                .thenThrow(new ResourceNotFoundException("Project", "id", projectId));

        assertThrows(ResourceNotFoundException.class, () -> {
            projectController.updateProject(projectId, updateRequest);
        });
    }

    @Test
    void updateProjectStatus_Success() {
        Jwt jwt = mock(Jwt.class);

        Project updatedProject = Project.builder()
                .id(projectId)
                .name("测试项目")
                .description("这是一个测试项目")
                .status(ProjectStatus.IN_PROGRESS)
                .workspaceId(workspaceId)
                .build();

        when(projectWriteService.updateProjectStatus(projectId, ProjectStatus.IN_PROGRESS))
                .thenReturn(updatedProject);

        ResponseEntity<ProjectResponse> response = projectController.updateProjectStatus(projectId, statusRequest, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateProjectStatus_NotFound() {
        Jwt jwt = mock(Jwt.class);

        when(projectWriteService.updateProjectStatus(projectId, ProjectStatus.IN_PROGRESS))
                .thenThrow(new ResourceNotFoundException("Project", "id", projectId));

        assertThrows(ResourceNotFoundException.class, () -> {
            projectController.updateProjectStatus(projectId, statusRequest, jwt);
        });
    }

    @Test
    void deleteProject_Success() {
        ResponseEntity<Void> response = projectController.deleteProject(projectId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(projectWriteService).deleteProject(projectId);
    }

    @Test
    void deleteProject_NotFound() {
        doThrow(new ResourceNotFoundException("Project", "id", projectId)).when(projectWriteService)
                .deleteProject(projectId);

        assertThrows(ResourceNotFoundException.class, () -> {
            projectController.deleteProject(projectId);
        });
    }
}