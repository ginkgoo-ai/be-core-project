package com.ginkgooai.core.project.domain.project;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import com.ginkgooai.core.project.domain.MemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "project_member")
public class ProjectMember extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;
}