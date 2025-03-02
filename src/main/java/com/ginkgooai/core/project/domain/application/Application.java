package com.ginkgooai.core.project.domain.application;

import com.ginkgooai.core.project.domain.talent.Talent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String workspaceId;

    private String projectId;

    private String roleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_id")
    private Talent talent;

    @OneToMany(mappedBy = "application")
    private List<ApplicationVideoMapping> videoMappings;

    private String agencyName;

    private String agentName;

    private String agentEmail;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String reviewedBy;
    
    private LocalDateTime reviewedAt;
    
    private String reviewNotes;

    private boolean shortlisted;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationNote> notes = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationComment> comments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}