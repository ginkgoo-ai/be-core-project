package com.ginkgooai.core.project.domain.project;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "project_nda")
public class ProjectNda extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Boolean requiresNda;

    private Boolean applyToAll;

    private String version;

    private String fullName;

    private String title;

    private String company;

    private String signatureUrl;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

}