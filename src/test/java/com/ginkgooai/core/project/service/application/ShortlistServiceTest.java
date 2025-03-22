package com.ginkgooai.core.project.service.application;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.client.identity.IdentityClient;
import com.ginkgooai.core.project.client.identity.dto.GuestCodeRequest;
import com.ginkgooai.core.project.client.identity.dto.GuestCodeResponse;
import com.ginkgooai.core.project.domain.application.Application;
import com.ginkgooai.core.project.domain.application.Shortlist;
import com.ginkgooai.core.project.domain.application.ShortlistItem;
import com.ginkgooai.core.project.domain.application.Submission;
import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.repository.ShortlistItemRepository;
import com.ginkgooai.core.project.repository.ShortlistRepository;
import com.ginkgooai.core.project.repository.SubmissionRepository;
import com.ginkgooai.core.project.service.ActivityLoggerService;

@ExtendWith(MockitoExtension.class)
class ShortlistServiceTest {

        @Mock
        private ShortlistRepository shortlistRepository;

        @Mock
        private ShortlistItemRepository shortlistItemRepository;

        @Mock
        private SubmissionRepository submissionRepository;

        @Mock
        private IdentityClient identityClient;

        @Mock
        private ActivityLoggerService activityLogger;

        @InjectMocks
        private ShortlistService shortlistService;

        private ShareShortlistRequest request;
        private List<Submission> submissions;
        private Application application;
        private Project project;
        private GuestCodeResponse guestCodeResponse;
        private MockedStatic<ContextUtils> contextUtilsMockedStatic;

        @BeforeEach
        void setUp() {
                contextUtilsMockedStatic = mockStatic(ContextUtils.class);
                contextUtilsMockedStatic.when(ContextUtils::getUserId).thenReturn(new String("user-1"));
                contextUtilsMockedStatic.when(ContextUtils::getWorkspaceId).thenReturn(new String("workspace-1"));

                project = Project.builder()
                                .id("project-1")
                                .build();

                application = Application.builder()
                                .id("application-1")
                                .project(project)
                                .build();

                submissions = Arrays.asList(
                                Submission.builder()
                                                .id("submission-1")
                                                .application(application)
                                                .build(),
                                Submission.builder()
                                                .id("submission-2")
                                                .application(application)
                                                .build());

                request = ShareShortlistRequest.builder()
                                .submissionIds(Arrays.asList("submission-1", "submission-2"))
                                .recipients(Arrays.asList(
                                                ShareShortlistRequest.Recipient.builder()
                                                                .email("test1@example.com")
                                                                .name("Test User 1")
                                                                .build(),
                                                ShareShortlistRequest.Recipient.builder()
                                                                .email("test2@example.com")
                                                                .name("Test User 2")
                                                                .build()))
                                .expiresInDays(7)
                                .build();

                guestCodeResponse = GuestCodeResponse.builder()
                                .guestCode("guest-code-1")
                                .build();
        }

        @AfterEach
        void tearDown() {
                if (contextUtilsMockedStatic != null) {
                        contextUtilsMockedStatic.close();
                }
        }

        @Test
        void shareShortlist_Success() {
                when(submissionRepository.findAllById(any())).thenReturn(submissions);
                when(shortlistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(identityClient.generateGuestCode(any())).thenReturn(ResponseEntity.ok(guestCodeResponse));

                Map<String, String> result = shortlistService.shareShortlist(request, "user-1");

                assertNotNull(result);
                assertEquals(2, result.size());
                assertTrue(result.containsKey("test1@example.com"));
                assertTrue(result.containsKey("test2@example.com"));

                verify(shortlistRepository, times(2)).save(any(Shortlist.class));
                verify(shortlistItemRepository, times(2)).save(any(ShortlistItem.class));
                verify(identityClient, times(2)).generateGuestCode(any(GuestCodeRequest.class));
        }

        @Test
        void shareShortlist_NoSubmissionsFound() {
                when(submissionRepository.findAllById(any())).thenReturn(List.of());

                assertThrows(ResourceNotFoundException.class, () -> shortlistService.shareShortlist(request, "user-1"));
        }

        @Test
        void shareShortlist_EmptyRecipients() {
                request.setRecipients(List.of());
                when(submissionRepository.findAllById(any())).thenReturn(submissions);

                Map<String, String> result = shortlistService.shareShortlist(request, "user-1");

                assertNotNull(result);
                assertTrue(result.isEmpty());
        }

        @Test
        void shareShortlist_WithCustomExpiryDays() {
                request.setExpiresInDays(14);
                when(submissionRepository.findAllById(any())).thenReturn(submissions);
                when(shortlistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(identityClient.generateGuestCode(any())).thenReturn(ResponseEntity.ok(guestCodeResponse));

                Map<String, String> result = shortlistService.shareShortlist(request, "user-1");

                assertNotNull(result);
                assertEquals(2, result.size());

                verify(identityClient, times(2))
                                .generateGuestCode(argThat(request -> request.getExpiryHours() == 14 * 24));
        }

        @Test
        void shareShortlist_WithNullExpiryDays() {
                request.setExpiresInDays(null);
                when(submissionRepository.findAllById(any())).thenReturn(submissions);
                when(shortlistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(identityClient.generateGuestCode(any())).thenReturn(ResponseEntity.ok(guestCodeResponse));

                Map<String, String> result = shortlistService.shareShortlist(request, "user-1");

                assertNotNull(result);
                assertEquals(2, result.size());

                verify(identityClient, times(2))
                                .generateGuestCode(argThat(request -> request.getExpiryHours() == 7 * 24));
        }

        @Test
        void shareShortlist_WithNullSubmissionIds() {
                request.setSubmissionIds(null);

                assertThrows(NullPointerException.class, () -> shortlistService.shareShortlist(request, "user-1"));
        }

        @Test
        void shareShortlist_WithEmptySubmissionIds() {
                request.setSubmissionIds(Collections.emptyList());

                assertThrows(ResourceNotFoundException.class, () -> shortlistService.shareShortlist(request, "user-1"));
        }

        @Test
        void shareShortlist_WithDuplicateSubmissionIds() {
                request.setSubmissionIds(Arrays.asList("submission-1", "submission-1"));
                when(submissionRepository.findAllById(any())).thenReturn(Collections.singletonList(submissions.get(0)));
                when(shortlistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(identityClient.generateGuestCode(any())).thenReturn(ResponseEntity.ok(guestCodeResponse));

                Map<String, String> result = shortlistService.shareShortlist(request, "user-1");

                assertNotNull(result);
                assertEquals(2, result.size());

                verify(submissionRepository).findAllById(argThat(ids -> {
                        Collection<String> idCollection = (Collection<String>) ids;
                        return idCollection.size() == 2
                                && idCollection.contains("submission-1");
                }));
        }

        @Test
        void shareShortlist_WithIdentityClientFailure() {
                when(submissionRepository.findAllById(any())).thenReturn(submissions);
                when(shortlistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(identityClient.generateGuestCode(any())).thenThrow(new RestClientException("API调用失败"));

                assertThrows(RestClientException.class, () -> shortlistService.shareShortlist(request, "user-1"));

                verify(shortlistRepository, times(1)).save(any(Shortlist.class));
        }

        @Test
        void shareShortlist_WithRepositorySaveFailure() {
                when(submissionRepository.findAllById(any())).thenReturn(submissions);
                when(shortlistRepository.save(any())).thenThrow(new RuntimeException("数据库操作失败"));

                assertThrows(RuntimeException.class, () -> shortlistService.shareShortlist(request, "user-1"));
        }

        @Test
        void shareShortlist_WithDuplicateRecipientEmails() {
                request.setRecipients(Arrays.asList(
                                ShareShortlistRequest.Recipient.builder()
                                                .email("same@example.com")
                                                .name("User 1")
                                                .build(),
                                ShareShortlistRequest.Recipient.builder()
                                                .email("same@example.com")
                                                .name("User 2")
                                                .build()));

                when(submissionRepository.findAllById(any())).thenReturn(submissions);
                when(shortlistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
                when(identityClient.generateGuestCode(any())).thenReturn(ResponseEntity.ok(guestCodeResponse));

                Map<String, String> result = shortlistService.shareShortlist(request, "user-1");

                assertNotNull(result);
                assertEquals(1, result.size());
                assertTrue(result.containsKey("same@example.com"));

                verify(shortlistRepository, times(2)).save(any(Shortlist.class));
        }
}