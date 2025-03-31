package com.ginkgooai.core.project.service;

import com.ginkgooai.core.common.constant.MessageQueue;
import com.ginkgooai.core.common.enums.ActivityType;
import com.ginkgooai.core.common.message.ActivityLogMessage;
import com.ginkgooai.core.common.queue.QueueInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLoggerService {

    private final QueueInterface queueInterface;

    public void log(String workspaceId,
            String projectId,
            String applicationId,
            ActivityType activityType,
            Map<String, Object> variables,
            Map<String, Object> attachments,
            String createdBy) {
        try {

            ActivityLogMessage message = ActivityLogMessage.builder()
                    .workspaceId(workspaceId)
                    .projectId(projectId)
                    .applicationId(applicationId)
                    .activityType(activityType.name())
                    .variables(variables)
                    .attachments(attachments)
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now(ZoneId.systemDefault()))
                    .build();

            queueInterface.send(MessageQueue.ACTIVITY_LOG_QUEUE, message);

            log.debug("Activity log message enqueued successfully for type: {}", activityType);
        } catch (Exception e) {
            log.error("Failed to enqueue activity log message for type: {}", activityType, e);
        }
    }
}