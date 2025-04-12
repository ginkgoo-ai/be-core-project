package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.constant.MessageQueue;
import com.ginkgooai.core.common.message.InnerMailSendMessage;
import com.ginkgooai.core.common.queue.QueueInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendEmailInnerService {

    private final QueueInterface queueInterface;
    private final EmailRateLimitService emailRateLimitService;

    public void email(InnerMailSendMessage message) {
        List<InnerMailSendMessage.Receipt> filteredReceipts = new ArrayList<>();
        String emailType = message.getEmailTemplateType();

        // Check rate limit for each recipient and filter out rate-limited ones
        for (InnerMailSendMessage.Receipt receipt : message.getReceipts()) {
            String recipientEmail = receipt.getTo();

            if (emailRateLimitService.isRateLimited(recipientEmail, emailType)) {
                log.warn("Email rate limit exceeded for recipient: {} with type: {}",
                        recipientEmail, emailType);
                continue;
            }

            // Set rate limit for this recipient and email type
            emailRateLimitService.setRateLimit(recipientEmail, emailType);
            filteredReceipts.add(receipt);
        }

        // Only send if there are non-rate-limited recipients
        if (!filteredReceipts.isEmpty()) {
            InnerMailSendMessage filteredMessage = InnerMailSendMessage.builder()
                    .emailTemplateType(emailType).receipts(filteredReceipts).build();
            queueInterface.send(MessageQueue.EMAIL_SEND_QUEUE, filteredMessage);
        } else {
            log.warn("All recipients were rate-limited for email type: {}", emailType);
        }
    }
}
