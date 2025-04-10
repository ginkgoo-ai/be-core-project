package com.ginkgooai.core.project.domain.talent;

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
@Table(name = "talent_comment")
public class TalentComment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String workspaceId;

    @Column(nullable = false)
    private String talentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = true)
    private String parentId;
}