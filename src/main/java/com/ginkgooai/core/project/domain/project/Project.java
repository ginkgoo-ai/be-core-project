package com.ginkgooai.core.project.domain.project;

import com.ginkgooai.core.project.domain.BaseAuditableEntity;
import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.dto.request.ProjectCreateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project")
public class Project extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private String plotLine;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.DRAFTING;

    private String producer;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectNda> ndas = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    private String workspaceId;

    private String posterUrl;

    public Project(ProjectCreateRequest request, String workspaceId, String userId) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.plotLine = request.getPlotLine();
        this.status = ProjectStatus.DRAFTING;
        this.posterUrl = request.getPosterUrl();
        this.workspaceId = workspaceId;
    }

    public void updateDetails(String name, String description, String plotLine, ProjectStatus status,
                              String posterUrl, String producer) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        this.name = name.trim();
        this.description = description;
        this.plotLine = plotLine;
        if (status != null) {
            this.status = status;
        }
        this.posterUrl = posterUrl;
        this.producer = producer;
    }

    public void addRole(ProjectRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        role.setProject(this);
        roles.add(role);
    }

    public void removeRole(String roleId) {
        roles.removeIf(role -> role.getId().equals(roleId));
    }

}