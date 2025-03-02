package com.ginkgooai.core.project.controller;

import com.ginkgooai.core.common.constant.RedisKey;
import com.ginkgooai.core.common.context.WorkspaceContext;
import com.ginkgooai.core.project.domain.talent.Talent;
import com.ginkgooai.core.project.dto.request.TalentCreateRequest;
import com.ginkgooai.core.project.dto.request.TalentSearchRequest;
import com.ginkgooai.core.project.dto.response.TalentResponse;
import com.ginkgooai.core.project.service.application.TalentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talents")
@Tag(name = "Talent Management", description = "APIs for managing talents")
@RequiredArgsConstructor
public class TalentController {

    private final TalentService talentService;

    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping
    @Operation(summary = "Create new talent from profiles")
    public ResponseEntity<TalentResponse> createTalent(@RequestBody TalentCreateRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        Talent talent = talentService.createTalentFromProfiles(request, jwt.getSubject());
        return ResponseEntity.ok(TalentResponse.from(talent));
    }

    @PostMapping("/{id}/refresh")
    @Operation(summary = "Refresh talent profiles")
    public ResponseEntity<TalentResponse> refreshProfiles(@PathVariable String id,
                                                          @AuthenticationPrincipal Jwt jwt) {
        Talent talent = talentService.refreshTalentProfiles(id, jwt.getSubject());
        return ResponseEntity.ok(TalentResponse.from(talent));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get talent by ID")
    public ResponseEntity<TalentResponse> getTalent(@PathVariable String id) {
        TalentResponse talent = talentService.getTalentById(id);
        return ResponseEntity.ok(talent);
    }

    @GetMapping
    @Operation(summary = "Search talents")
    public ResponseEntity<Page<TalentResponse>> searchTalents(TalentSearchRequest request,
                                                              Pageable pageable,
                                                              @AuthenticationPrincipal Jwt jwt) {

        String key = RedisKey.WORKSPACE_CONTEXT_KEY_PREFIX + jwt.getSubject();
        String workspaceId = redisTemplate.opsForValue().get(key);
//        String workspaceId = WorkspaceContext.getWorkspaceId(); 
        request.setWorkspaceId(workspaceId);

        return ResponseEntity.ok(talentService.searchTalents(request, pageable));
    }
}