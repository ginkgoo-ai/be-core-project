package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submission")
public class Submission extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToMany(mappedBy = "submissions")
    private List<ShortlistItem> shortlistItems;

    private String videoName;

    private String videoUrl;

    private String videoThumbnailUrl;

    private Long videoDuration;

    private String videoResolution;

    private Long fileSize;

    private String mimeType;

    @Enumerated(EnumType.STRING)
    private SubmissionProcessingStatus processingStatus;

    private String processingError;

    private String originalFilename;

    @Builder.Default
    private Long viewCount = 0L;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubmissionComment> comments = new ArrayList<>();

}