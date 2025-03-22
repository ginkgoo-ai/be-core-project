package com.ginkgooai.core.project.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.BatchShareShortlistResponse;
import com.ginkgooai.core.project.service.application.ShortlistService;

@ExtendWith(MockitoExtension.class)
class ShortlistControllerTest {

    @Mock
    private ShortlistService shortlistService;

    @InjectMocks
    private ShortlistController shortlistController;

    private ShareShortlistRequest request;
    private Map<String, String> shareLinks;
    private MockedStatic<ContextUtils> contextUtilsMockedStatic;

    @BeforeEach
    void setUp() {
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

        shareLinks = new HashMap<>();
        shareLinks.put("test1@example.com", "http://example.com/link1");
        shareLinks.put("test2@example.com", "http://example.com/link2");

        contextUtilsMockedStatic = mockStatic(ContextUtils.class);
        contextUtilsMockedStatic.when(ContextUtils::getUserId).thenReturn("user-1");
    }

    @AfterEach
    void tearDown() {
        if (contextUtilsMockedStatic != null) {
            contextUtilsMockedStatic.close();
        }
    }

    @Test
    void shareShortlist_Success() {
        when(shortlistService.shareShortlist(any(), eq("user-1"))).thenReturn(shareLinks);

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getSuccessCount());
        assertEquals(shareLinks, response.getBody().getShareLinks());
        verify(shortlistService).shareShortlist(request, "user-1");
    }

    @Test
    void shareShortlist_EmptyResult() {
        when(shortlistService.shareShortlist(any(), eq("user-1"))).thenReturn(new HashMap<>());

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertTrue(response.getBody().getShareLinks().isEmpty());
    }

    @Test
    void shareShortlist_WithResourceNotFoundException() {
        when(shortlistService.shareShortlist(any(), eq("user-1")))
                .thenThrow(new ResourceNotFoundException("Submissions", "ids", "submission-1"));

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shareShortlist_WithIllegalArgumentException() {
        when(shortlistService.shareShortlist(any(), eq("user-1")))
                .thenThrow(new IllegalArgumentException("Invalid arguments"));

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shareShortlist_WithRuntimeException() {
        when(shortlistService.shareShortlist(any(), eq("user-1")))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shareShortlist_WithNullUser() {
        contextUtilsMockedStatic.when(ContextUtils::getUserId).thenReturn(null);

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shareShortlist_WithEmptyUser() {
        contextUtilsMockedStatic.when(ContextUtils::getUserId).thenReturn("");

        ResponseEntity<BatchShareShortlistResponse> response = shortlistController.shareShortlist(request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}