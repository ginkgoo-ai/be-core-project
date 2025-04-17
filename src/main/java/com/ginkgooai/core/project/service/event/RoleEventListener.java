package com.ginkgooai.core.project.service.event;

import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.project.domain.event.RoleStatusChangedEvent;
import com.ginkgooai.core.project.service.ActivityLoggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleEventListener {

	private final ActivityLoggerService activityLogger;

	@EventListener
	public void handleRoleStatusChangedEvent(RoleStatusChangedEvent event) {
		log.debug("Role status changed: {} -> {}, roleId: {}", event.getOldStatus(), event.getNewStatus(),
				event.getRole().getId());

		activityLogger.log(event.getRole().getWorkspaceId(), event.getRole().getProject().getId(), null,
				ActivityType.ROLE_STATUS_UPDATE,
				Map.of("roleName", event.getRole().getName(), "newStatus", event.getNewStatus().getValue()), null,
				event.getUserId());
	}

}