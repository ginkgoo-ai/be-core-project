package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.constant.MessageQueue;
import com.ginkgooai.core.common.message.InnerMailSendMessage;
import com.ginkgooai.core.common.queue.QueueInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendEmailInnerServiceTest {

    @Mock
    private QueueInterface queueInterface;

    @Mock
    private EmailRateLimitService emailRateLimitService;

    private SendEmailInnerService sendEmailInnerService;

    @BeforeEach
    void setUp() {
        sendEmailInnerService = new SendEmailInnerService(queueInterface, emailRateLimitService);
    }

    @Test
    void testEmail_WhenNoRateLimit_ShouldSendAllEmails() {
        // Arrange
        String emailType = "INVITATION";
        List<InnerMailSendMessage.Receipt> receipts = Arrays
            .asList(createReceipt("test1@example.com"), createReceipt("test2@example.com"));
        InnerMailSendMessage message = createMessage(emailType, receipts);

        when(emailRateLimitService.isRateLimited(anyString(), anyString())).thenReturn(false);

        // Act
        sendEmailInnerService.email(message);

        // Assert
        verify(emailRateLimitService, times(2)).isRateLimited(anyString(), eq(emailType));
        verify(emailRateLimitService, times(2)).setRateLimit(anyString(), eq(emailType));
        verify(queueInterface).send(eq(MessageQueue.EMAIL_SEND_QUEUE), eq(message));
    }

    @Test
    void testEmail_WhenSomeRecipientsRateLimited_ShouldSkipThoseRecipients() {
        // Arrange
        String emailType = "INVITATION";
        List<InnerMailSendMessage.Receipt> receipts = Arrays
            .asList(createReceipt("test1@example.com"), createReceipt("test2@example.com"));
        InnerMailSendMessage message = createMessage(emailType, receipts);

        when(emailRateLimitService.isRateLimited("test1@example.com", emailType)).thenReturn(true);
        when(emailRateLimitService.isRateLimited("test2@example.com", emailType)).thenReturn(false);

        // Act
        sendEmailInnerService.email(message);

        // Assert
        verify(emailRateLimitService).isRateLimited("test1@example.com", emailType);
        verify(emailRateLimitService).isRateLimited("test2@example.com", emailType);
        verify(emailRateLimitService).setRateLimit("test2@example.com", emailType);

        // Create expected message with only non-rate-limited recipients
        InnerMailSendMessage expectedMessage =
            createMessage(emailType, Arrays.asList(createReceipt("test2@example.com")));
        verify(queueInterface).send(eq(MessageQueue.EMAIL_SEND_QUEUE), eq(expectedMessage));
    }

    @Test
    void testEmail_WhenAllRecipientsRateLimited_ShouldNotSendMessage() {
        // Arrange
        String emailType = "INVITATION";
        List<InnerMailSendMessage.Receipt> receipts = Arrays
            .asList(createReceipt("test1@example.com"), createReceipt("test2@example.com"));
        InnerMailSendMessage message = createMessage(emailType, receipts);

        when(emailRateLimitService.isRateLimited(anyString(), anyString())).thenReturn(true);

        // Act
        sendEmailInnerService.email(message);

        // Assert
        verify(emailRateLimitService, times(2)).isRateLimited(anyString(), eq(emailType));
        verify(emailRateLimitService, never()).setRateLimit(anyString(), anyString());
        verify(queueInterface, never()).send(anyString(), any());
    }

    private InnerMailSendMessage.Receipt createReceipt(String email) {
        return InnerMailSendMessage.Receipt.builder().to(email).build();
    }

    private InnerMailSendMessage createMessage(String emailType,
                                               List<InnerMailSendMessage.Receipt> receipts) {
        return InnerMailSendMessage.builder().emailTemplateType(emailType).receipts(receipts)
            .build();
    }
}
