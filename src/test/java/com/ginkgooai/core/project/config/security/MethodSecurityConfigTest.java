package com.ginkgooai.core.project.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ginkgooai.core.project.filter.WorkspaceAuthFilter;
import com.ginkgooai.core.project.service.ProjectWorkspaceContextService;
import com.ginkgooai.core.project.service.application.ShortlistService;

/**
 * 安全性测试的简化版控制器 - 仅用于测试
 */
@RestController
class TestSecurityController {

    @GetMapping("/shortlists/{shortlistId}/items")
    @PreAuthorize("hasRole('ROLE_USER') or (hasRole('ROLE_GUEST') and (hasAuthority('shortlist:' + #shortlistId + ':read') or hasAuthority('shortlist:' + #shortlistId + ':write')))")
    public ResponseEntity<?> getShortlistItemsByShortlistId(@PathVariable String shortlistId) {
        return ResponseEntity.ok().build();
    }
}

@WebAppConfiguration
@WebMvcTest(controllers = TestSecurityController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        WorkspaceAuthFilter.class,
        SecurityConfig.class
}))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class MethodSecurityConfigTest {

    @Autowired
    private TestSecurityController securityController;

    @MockBean
    private ShortlistService shortlistService;

    @MockBean
    private ProjectWorkspaceContextService projectWorkspaceContextService;

    @MockBean
    private JwtDecoder jwtDecoder; // 添加JwtDecoder的Mock

    @Test
    void verifyControllerHasSecurityProxy() {
        // 验证控制器是否被代理，这表明方法级安全已激活
        assertNotNull(securityController);
        assertEquals(true, AopUtils.isAopProxy(securityController),
                "Controller should be proxied for method security");
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessWithRoleUser() {
        String shortlistId = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e";
        ResponseEntity<?> response = securityController.getShortlistItemsByShortlistId(shortlistId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testAccessWithGuestAndScope() {
        String shortlistId = "cfc08cb3-c87c-4190-9355-1ff73fe15c0e";

        Authentication authentication = mock(Authentication.class);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        authorities.add(new SimpleGrantedAuthority("shortlist:" + shortlistId + ":read"));

        doReturn(authorities).when(authentication).getAuthorities();
        doReturn(true).when(authentication).isAuthenticated();

        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<?> response = securityController.getShortlistItemsByShortlistId(shortlistId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        SecurityContextHolder.clearContext();
    }
}