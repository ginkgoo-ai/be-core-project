package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.constant.MessageQueue;
import com.ginkgooai.core.common.message.InnerMailSendMessage;
import com.ginkgooai.core.common.queue.QueueInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendEmailInnerService {

    private final QueueInterface queueInterface;
    private final EmailRateLimitService emailRateLimitService;

    public void email(InnerMailSendMessage message) {
        // Check rate limit for each recipient
        for (InnerMailSendMessage.Receipt receipt : message.getReceipts()) {
            String recipientEmail = receipt.getTo();
            String emailType = message.getEmailTemplateType();

            if (emailRateLimitService.isRateLimited(recipientEmail, emailType)) {
                log.warn("Email rate limit exceeded for recipient: {} with type: {}",
                    recipientEmail, emailType);
                continue;
            }

            // Set rate limit for this recipient and email type
            emailRateLimitService.setRateLimit(recipientEmail, emailType);
        }

        queueInterface.send(MessageQueue.EMAIL_SEND_QUEUE, message);
    }
}
