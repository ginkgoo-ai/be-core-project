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
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "project",
        indexes = {
                @Index(name = "idx_owner", columnList = "owner_id"),
                @Index(name = "idx_createdat", columnList = "created_at")
        }
)
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

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectNda> ndas = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectActivity> activities = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Project createFromRequest(ProjectRequest request) {
        Project project = new Project(
                request.getName(),
                request.getDescription(),
                request.getPlotLine(),
                request.getOwnerId()
        );

     
        
        return project;
    }

    // Constructor for creating a new project (used in write operations)
    public Project(String name, String description, String plotLine, String ownerId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID cannot be null or empty");
        }
        this.name = name.trim();
        this.description = description;
        this.plotLine = plotLine;
        this.ownerId = ownerId;
        this.status = ProjectStatus.DRAFTING;
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
        logActivity(ActivityType.NDA_SIGNED, "NDA added for project");
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
        logActivity(ActivityType.MEMBER_ADDED, "Member " + member.getUserId() + " added");
    }

    public void removeMember(String userId) {
        members.removeIf(member -> member.getUserId().equals(userId));
    }

    public void addActivity(ProjectActivity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        activity.setProject(this);
        activities.add(activity);
        this.lastActivityAt = LocalDateTime.now();
    }

    private void logActivity(ActivityType type, String description) {
        ProjectActivity activity = new ProjectActivity();
        activity.setActivityType(type);
        activity.setStatus(ActivityStatus.SUBMITTED);
        activity.setDescription(description);
        activity.setCreatedAt(LocalDateTime.now());
        addActivity(activity);
    }

    public boolean isMember(String userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }

}