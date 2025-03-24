package com.ginkgooai.core.project.domain.application;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Records video viewing history for deduplication and analytics
 */
@Entity
@Table(name = "submission_view_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionViewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    /**
     * User ID of the viewer, null for guest
     */
    private String userId;

    /**
     * IP address of the viewer
     */
    private String ipAddress;

    /**
     * User-Agent information (optional)
     */
    private String userAgent;

    /**
     * Timestamp when the video was viewed
     */
    @CreationTimestamp
    private LocalDateTime viewedAt;
}