package com.ginkgooai.core.project.domain.project;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.domain.BaseLogicalDeleteEntity;
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
public class Project extends BaseLogicalDeleteEntity {

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

    public Project(ProjectCreateRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.plotLine = request.getPlotLine();
        this.status = ProjectStatus.DRAFTING;
        this.posterUrl = request.getPosterUrl();
        this.workspaceId = ContextUtils.getWorkspaceId();
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

    public static Project create(ProjectCreateRequest request, String workspaceId) {
        return Project.builder()
            .workspaceId(workspaceId)
            .name(request.getName())
            .description(request.getDescription())
            .status(ProjectStatus.DRAFTING)
            .build();
    }
    
    public void addRole(ProjectRole role) {
        roles.add(role);
        role.setProject(this);
    }

    public void removeRole(String roleId) {
        roles.removeIf(role -> role.getId().equals(roleId));
    }
}