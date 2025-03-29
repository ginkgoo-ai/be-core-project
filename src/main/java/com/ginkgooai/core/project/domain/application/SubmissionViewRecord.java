package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records video viewing history for deduplication and analytics
 */
@Entity
@Table(name = "submission_view_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionViewRecord extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    private String userId;

    private String ipAddress;

    private String userAgent;

    @CreationTimestamp
    private LocalDateTime viewedAt;
}