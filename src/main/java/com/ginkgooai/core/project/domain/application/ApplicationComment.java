package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.BaseLogicalDeleteEntity;
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
@Table(name = "application_comment")
public class ApplicationComment extends BaseLogicalDeleteEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private ApplicationComment parentComment;
}