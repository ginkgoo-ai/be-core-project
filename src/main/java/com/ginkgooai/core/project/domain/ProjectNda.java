package com.ginkgooai.core.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "project_nda")
public class ProjectNda {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}