package com.ginkgooai.core.project.domain.application;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submission_comment")
public class SubmissionComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String createdBy;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private SubmissionComment parentComment;
}