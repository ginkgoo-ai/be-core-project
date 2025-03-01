package com.ginkgooai.core.project.domain;

import com.ginkgooai.core.project.dto.request.ProjectRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private String plotLine;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.DRAFTING;

    private String ownerId;

    private String producer;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectNda> ndas = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    private String workspaceId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Project(ProjectRequest request, String workspaceId) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.plotLine = request.getPlotLine();
        this.ownerId = request.getOwnerId();
        this.status = ProjectStatus.DRAFTING;
        this.workspaceId = workspaceId;
        this.createdAt = LocalDateTime.now();
    }

    public void updateDetails(String name, String description, String plotLine, ProjectStatus status) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        this.name = name.trim();
        this.description = description;
        this.plotLine = plotLine;
        if (status != null) {
            this.status = status;
        }
        this.lastActivityAt = LocalDateTime.now();
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

    public void addNda(ProjectNda nda) {
        if (nda == null) {
            throw new IllegalArgumentException("NDA cannot be null");
        }
        nda.setProject(this);
        ndas.add(nda);
    }

    public void removeNda(String ndaId) {
        ndas.removeIf(nda -> nda.getId().equals(ndaId));
    }

    public void addMember(ProjectMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        member.setProject(this);
        members.add(member);
    }

    public void removeMember(String userId) {
        members.removeIf(member -> member.getUserId().equals(userId));
    }

    public boolean isMember(String userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }

}