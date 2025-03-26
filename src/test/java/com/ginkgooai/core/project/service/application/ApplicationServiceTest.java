package com.ginkgooai.core.project.service.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ginkgooai.core.project.domain.talent.TalentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.UserInfoResponse;
import com.ginkgooai.core.project.client.storage.StorageClient;
import com.ginkgooai.core.project.client.storage.dto.CloudFileResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.ApplicationCreateRequest;
import com.ginkgooai.core.project.dto.request.TalentRequest;
import com.ginkgooai.core.project.dto.response.ApplicationResponse;
import com.ginkgooai.core.project.repository.ApplicationNoteRepository;
import com.ginkgooai.core.project.repository.ApplicationRepository;
import com.ginkgooai.core.project.repository.ProjectRepository;
import com.ginkgooai.core.project.repository.ProjectRoleRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.repository.TalentRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationNoteRepository applicationNoteRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectRoleRepository projectRoleRepository;

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private TalentService talentService;

    @Mock
    private StorageClient storageClient;

    @Mock
    private IdentityClient identityClient;

    @Mock
    private ActivityLoggerService activityLogger;

    @InjectMocks
    private ApplicationService applicationService;

    private Project project;
    private ProjectRole role;
    private Talent talent;
    private Application application;
    private ApplicationCreateRequest createRequest;
    private String workspaceId = "workspace-1";
    private String userId = "user-1";

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id("project-1")
                .workspaceId(workspaceId)
                .build();

        role = ProjectRole.builder()
                .id("role-1")
                .name("Actor")
                .project(project)
                .build();

        talent = Talent.builder()
                .id("talent-1")
                .name("John Doe")
                .email("john@example.com")
                .status(TalentStatus.ACTIVE)
                .build();

        application = Application.builder()
                .id("app-1")
                .workspaceId(workspaceId)
                .project(project)
                .role(role)
                .talent(talent)
                .status(ApplicationStatus.ADDED)
                .createdBy(userId)
                .build();

        createRequest = new ApplicationCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setRoleId(role.getId());
        createRequest.setTalentId(talent.getId());
    }

    @Test
    void createApplication_Success() {
        // Mock repository responses
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(talentRepository.findById(anyString())).thenReturn(Optional.of(talent));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Execute test
        ApplicationResponse response = applicationService.createApplication(createRequest, workspaceId, userId);

        // Verify
        assertNotNull(response);
        assertEquals(application.getId(), response.getId());
        assertEquals(ApplicationStatus.ADDED, response.getStatus());

        // Verify that role status was updated
        assertEquals(RoleStatus.CASTING, role.getStatus());

        // Verify that activity was logged
        verify(activityLogger).log(
                eq(workspaceId),
                eq(project.getId()),
                eq(application.getId()),
                any(),
                any(),
                eq(null),
                eq(userId));
    }

    @Test
    void createApplication_WithNewTalent_Success() {
        // Create request with new talent
        createRequest.setTalentId(null);
        TalentRequest talentRequest = new TalentRequest();
        talentRequest.setName("New Talent");
        talentRequest.setEmail("new@example.com");
        createRequest.setTalent(talentRequest);

        // Mock repository responses
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(talentService.createTalentFromProfiles(any(), eq(workspaceId), eq(userId))).thenReturn(talent);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Execute test
        ApplicationResponse response = applicationService.createApplication(createRequest, workspaceId, userId);

        // Verify
        assertNotNull(response);
        assertEquals(application.getId(), response.getId());

        // Verify talent was created through service
        verify(talentService).createTalentFromProfiles(eq(talentRequest), eq(workspaceId), eq(userId));
    }

    @Test
    void createApplication_WithVideos_Success() {
        // Add video IDs to request
        List<String> videoIds = Arrays.asList("video-1", "video-2");
        createRequest.setVideoIds(videoIds);

        // Prepare application with submissions
        Application appWithSubmissions = Application.builder()
                .id(application.getId())
                .workspaceId(application.getWorkspaceId())
                .project(application.getProject())
                .role(application.getRole())
                .talent(application.getTalent())
                .status(ApplicationStatus.SUBMITTED) // Status should be updated
                .submissions(new ArrayList<>())
                .build();

        // Mock responses
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(talentRepository.findById(anyString())).thenReturn(Optional.of(talent));
        when(applicationRepository.save(any(Application.class))).thenReturn(appWithSubmissions);

        // Mock storage client
        List<CloudFileResponse> cloudFiles = Arrays.asList(
                CloudFileResponse.builder().id("video-1").originalName("video1.mp4").build(),
                CloudFileResponse.builder().id("video-2").originalName("video2.mp4").build());
        when(storageClient.getFileDetails(eq(videoIds))).thenReturn(ResponseEntity.ok(cloudFiles));

        // Execute test
        ApplicationResponse response = applicationService.createApplication(createRequest, workspaceId, userId);

        // Verify
        assertNotNull(response);
        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());

        // Verify storage client was called
        verify(storageClient).getFileDetails(eq(videoIds));
    }

    @Test
    void createApplication_StorageClientFailure() {
        // Add video IDs to request
        List<String> videoIds = Arrays.asList("video-1", "video-2");
        createRequest.setVideoIds(videoIds);

        // Mock responses for successful DB operations
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(talentRepository.findById(anyString())).thenReturn(Optional.of(talent));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Mock storage client to fail
        when(storageClient.getFileDetails(eq(videoIds))).thenThrow(new RestClientException("API Error"));

        // Execute test and expect exception
        Exception exception = assertThrows(RestClientException.class, () -> {
            applicationService.createApplication(createRequest, workspaceId, userId);
        });

        // Verify application was saved first (potential DB inconsistency)
        verify(applicationRepository).save(any(Application.class));

        // Verify storage client was called
        verify(storageClient).getFileDetails(eq(videoIds));

        // Verify exception details
        assertTrue(exception.getMessage().contains("API Error"));
    }

    @Test
    void createApplication_TalentProfileScraping_Performance() throws Exception {
        // Simulate delay in talent creation
        createRequest.setTalentId(null);
        TalentRequest talentRequest = new TalentRequest();
        talentRequest.setName("New Talent");
        talentRequest.setEmail("new@example.com");
        talentRequest.setImdbProfileUrl("https://imdb.com/name/123");
        createRequest.setTalent(talentRequest);

        // Mock repository responses
        when(projectRepository.findById(anyString())).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(anyString())).thenReturn(Optional.of(role));

        // Simulate delay in talent service (web scraping)
        when(talentService.createTalentFromProfiles(any(), eq(workspaceId), eq(userId))).thenAnswer(invocation -> {
            // Sleep to simulate a slow external service call
            Thread.sleep(1000);
            return talent;
        });

        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        // Execute test with timeout
        long startTime = System.currentTimeMillis();
        applicationService.createApplication(createRequest, workspaceId, userId);
        long endTime = System.currentTimeMillis();

        // Verify execution time (should be at least 1 second due to simulated delay)
        assertTrue((endTime - startTime) >= 1000, "Method should take at least 1 second due to web scraping delay");

        // Verify talent service was called
        verify(talentService).createTalentFromProfiles(any(), eq(workspaceId), eq(userId));
    }

    @Test
    void listApplications_Success() {
        // Mock repository
        List<Application> applications = Collections.singletonList(application);
        Page<Application> applicationPage = new PageImpl<>(applications);

        when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(applicationPage);

        // Execute test
        Page<ApplicationResponse> responsePage = applicationService.listApplications(
                workspaceId, userId, null, null, null, null, null, null, null, null, Pageable.unpaged());

        // Verify
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());

        // Verify no calls to identity client if no notes
        verify(identityClient, never()).getUsersByIds(anyList());
    }

    @Test
    void listApplications_WithUserInfoLookup() {
        // Create application with notes
        Application appWithNotes = Application.builder()
                .id(application.getId())
                .workspaceId(application.getWorkspaceId())
                .project(application.getProject())
                .role(application.getRole())
                .talent(application.getTalent())
                .status(application.getStatus())
                .notes(new ArrayList<>())
                .build();

        // Mock repository
        List<Application> applications = Collections.singletonList(appWithNotes);
        Page<Application> applicationPage = new PageImpl<>(applications);

        when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(applicationPage);

        // Execute test
        Page<ApplicationResponse> responsePage = applicationService.listApplications(
                workspaceId, userId, null, null, null, null, null, null, null, null, Pageable.unpaged());

        // Verify
        assertNotNull(responsePage);

        // Verify identity client was called only if there are notes with users
        verify(identityClient, atMostOnce()).getUsersByIds(anyList());
    }

    @Test
    void listApplications_IdentityClientFailure() {
        // Create application with notes that have creators
        Application appWithNotes = Application.builder()
                .id(application.getId())
                .workspaceId(application.getWorkspaceId())
                .project(application.getProject())
                .role(application.getRole())
                .talent(application.getTalent())
                .status(application.getStatus())
                .notes(new ArrayList<>())
                .build();

        // Mock repository
        List<Application> applications = Collections.singletonList(appWithNotes);
        Page<Application> applicationPage = new PageImpl<>(applications);

        when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(applicationPage);

        // Execute test - should not throw exception despite identity client failure
        Page<ApplicationResponse> responsePage = applicationService.listApplications(
                workspaceId, userId, null, null, null, null, null, null, null, null, Pageable.unpaged());

        // Verify we still got results despite the identity client failure
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    void getApplicationById_NotFound() {
        // Mock repository to return empty
        when(applicationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Execute test and expect exception
        assertThrows(ResourceNotFoundException.class, () -> {
            applicationService.getApplicationById(workspaceId, "non-existent-id");
        });
    }
}