package com.ginkgooai.core.project.domain.role;

import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.core.project.component.ApplicationContextProvider;
import com.ginkgooai.core.project.domain.BaseLogicalDeleteEntity;
import com.ginkgooai.core.project.domain.event.RoleStatusChangedEvent;
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
public class ProjectRole extends BaseLogicalDeleteEntity {

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

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String workspaceId;

	public ProjectRole(Project project, String name, String characterDescription, String selfTapeInstructions,
			String[] sides) {
		this.name = name;
		this.characterDescription = characterDescription;
		this.selfTapeInstructions = selfTapeInstructions;
		this.sides = sides;
		this.status = RoleStatus.DRAFTING;
		this.project = project;
		this.workspaceId = project.getWorkspaceId();
	}

	public void setStatus(RoleStatus newStatus) {
		if (newStatus != this.status) {
			RoleStatus oldStatus = this.status;
			this.status = newStatus;

			String userId = ContextUtils.getUserId();
			ApplicationContextProvider.getApplicationContext()
				.publishEvent(new RoleStatusChangedEvent(this, this, oldStatus, newStatus, userId));
		}
	}

}