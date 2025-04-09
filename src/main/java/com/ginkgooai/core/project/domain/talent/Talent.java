package com.ginkgooai.core.project.domain.talent;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import com.ginkgooai.core.project.dto.request.TalentRequest;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "talent")
public class Talent extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    private String firstName;

    private String lastName;

    private String nameSuffix;

    private String email;

    private String imdbProfileUrl;

    private String spotlightProfileUrl;

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

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Contact> contacts;

    @Enumerated(EnumType.STRING)
    private TalentStatus status;

    @Column
    private Long applicationCount = 0L;

    @Column
    private Long submissionCount = 0L;

    public static Talent from(TalentRequest request) {
        return Talent.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .nameSuffix(request.getNameSuffix())
            .email(request.getEmail())
            .imdbProfileUrl(request.getImdbProfileUrl())
            .spotlightProfileUrl(request.getSpotlightProfileUrl())
            .profilePhotoUrl(request.getProfilePhotoUrl())
            .status(TalentStatus.ACTIVE)
            .contacts(request.getContacts())
            .workspaceId(ContextUtils.getWorkspaceId())
            .build();
    }

    public void incrementApplicationCount() {
        this.applicationCount = (this.applicationCount == null ? 0 : this.applicationCount) + 1;
    }

    public void decrementApplicationCount() {
        this.applicationCount = (this.applicationCount == null || this.applicationCount < 1) ? 0 : this.applicationCount - 1;
    }

    public void incrementSubmissionCount() {
        this.submissionCount = (this.submissionCount == null ? 0 : this.submissionCount) + 1;
    }
    
    public void decrementSubmissionCount() {
        this.submissionCount = (this.submissionCount == null || this.submissionCount < 1) ? 0 : this.submissionCount - 1;
    }
}