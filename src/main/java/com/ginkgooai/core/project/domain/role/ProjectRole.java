package com.ginkgooai.core.project.domain.role;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import com.ginkgooai.core.project.domain.project.Project;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_role")
public class ProjectRole extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String characterDescription;

    private String selfTapeInstructions;

    @Column(name = "sides", columnDefinition = "text[]")
    private String[] sides;

    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private RoleStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}