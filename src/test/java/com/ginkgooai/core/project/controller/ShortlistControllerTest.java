package com.ginkgooai.core.project.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.dto.request.ShareShortlistRequest;
import com.ginkgooai.core.project.dto.response.BatchShareShortlistResponse;
import com.ginkgooai.core.project.dto.response.ShortlistItemResponse;
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
    private MockMvc mockMvc;

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

        mockMvc = MockMvcBuilders.standaloneSetup(shortlistController).build();
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
    void testGetShortlistItemsByShortlistId() throws Exception {
        String shortlistId = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e";
        Page<ShortlistItemResponse> mockPage = new PageImpl<>(
                List.of(ShortlistItemResponse.builder().id("id1").build()));

        when(shortlistService.listShortlistItemsByShortlistId(eq(shortlistId), eq(null), any(Pageable.class)))
                .thenReturn(mockPage);

        // 创建一个包含必要权限的Authentication对象
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        authorities.add(new SimpleGrantedAuthority("shortlist:" + shortlistId + ":read"));

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("guest@example.com", "password",
                authorities);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(MockMvcRequestBuilders.get("/shortlists/{shortlistId}/items", shortlistId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value("id1"));

        verify(shortlistService).listShortlistItemsByShortlistId(eq(shortlistId), eq(null), any(Pageable.class));
    }

    @Test
    void testGetShortlistItemsByShortlistId_Unauthorized() throws Exception {
        String shortlistId = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e";

        // 创建一个没有必要权限的Authentication对象
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 没有ROLE_GUEST角色

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user@example.com", "password",
                authorities);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(MockMvcRequestBuilders.get("/shortlists/{shortlistId}/items", shortlistId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        // 验证服务方法未被调用
        verify(shortlistService, org.mockito.Mockito.never()).listShortlistItemsByShortlistId(any(), any(), any());
    }
}