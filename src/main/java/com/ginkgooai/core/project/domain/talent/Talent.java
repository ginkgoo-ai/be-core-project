package com.ginkgooai.core.project.domain.talent;

import com.ginkgooai.core.project.dto.request.TalentRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import io.hypersistence.utils.hibernate.type.json.JsonType;


import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "talent")
public class Talent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;
    
    private String name;
    
    private String nameSuffix;
    
    private String email;
    
    private String imdbProfileUrl;
    
    private String spotlightProfileUrl;
    
    private String agencyName;
    
    private String agentName;
    
    private String agentEmail;

    private String profileMetaId;
    
    private String profilePhotoUrl;

    @Column(name = "known_for_movie_ids", columnDefinition = "text[]")
    private String[] knownForMovieIds;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> personalDetails;

    @Enumerated(EnumType.STRING)
    private TalentStatus status;

    private String createdBy;
    
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Talent from(TalentRequest request, String userid, String workspaceId) {
        return Talent.builder()
                .name(request.getName())
                .nameSuffix(request.getNameSuffix())
                .email(request.getEmail())
                .imdbProfileUrl(request.getImdbProfileUrl())
                .spotlightProfileUrl(request.getSpotlightProfileUrl())
                .agencyName(request.getAgencyName())
                .agentName(request.getAgentName())
                .agentEmail(request.getAgentEmail())
                .profilePhotoUrl(request.getProfilePhotoUrl())
                .status(TalentStatus.ACTIVE)
                .createdBy(userid)
                .workspaceId(workspaceId)
                .build();
    }
}