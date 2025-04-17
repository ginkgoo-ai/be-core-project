package com.ginkgooai.core.project.domain.event;

import com.ginkgooai.core.project.domain.role.ProjectRole;
import com.ginkgooai.core.project.domain.role.RoleStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoleStatusChangedEvent extends ApplicationEvent {

	private final ProjectRole role;

	private final RoleStatus oldStatus;

	private final RoleStatus newStatus;

	private final String userId;

	public RoleStatusChangedEvent(Object source, ProjectRole role, RoleStatus oldStatus, RoleStatus newStatus,
			String userId) {
		super(source);
		this.role = role;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.userId = userId;
	}

}