package com.ginkgooai.core.project.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ginkgooai.core.project.service.ProjectWorkspaceContextService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkspaceAuthFilter extends OncePerRequestFilter {

    private final ProjectWorkspaceContextService projectWorkspaceContextService;

    private static final List<String> EXCLUDE_PATH_PATTERNS = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/health",
            "/api/project/v3/api-docs",
            "/api/project/swagger-ui");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean isExcludedPath = EXCLUDE_PATH_PATTERNS.stream()
                .anyMatch(pattern -> path.startsWith(pattern));

        if (isExcludedPath) {
            return true;
        }

        return !path.startsWith("/api");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String workspaceId = request.getHeader("x-workspace-id");
        if (workspaceId == null || workspaceId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            boolean hasAccess = projectWorkspaceContextService.validateUserWorkspaceAccess(jwt.getSubject(),
                    workspaceId);

            if (!hasAccess) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Failed to validate workspace access: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}