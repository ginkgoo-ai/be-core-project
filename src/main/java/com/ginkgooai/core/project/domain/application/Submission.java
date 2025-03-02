package com.ginkgooai.core.project.domain.application;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "video_submission")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    private String videoUrl;
    
    private String thumbnailUrl;
    
    private Long duration;
    
    private String resolution;
    
    private Long fileSize;
    
    private String mimeType;
    
    @Enumerated(EnumType.STRING)
    private SubmissionProcessingStatus processingStatus;
    
    private String processingError;
    
    private String uploadedBy;
    
    private String originalFilename;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}