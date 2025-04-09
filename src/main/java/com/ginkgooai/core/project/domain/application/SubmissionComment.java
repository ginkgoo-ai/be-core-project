package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submission_comment")
public class SubmissionComment extends BaseAuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentType type;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submission submission;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private SubmissionComment parentComment;
}