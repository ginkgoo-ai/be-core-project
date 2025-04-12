package com.ginkgooai.core.project.service.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private EmailRateLimitService emailRateLimitService;

    @BeforeEach
    void setUp() {
        emailRateLimitService = new EmailRateLimitService(redisTemplate);
    }

    @Test
    void testIsRateLimited_WhenKeyExists_ReturnsTrue() {
        // Arrange
        String recipientEmail = "test@example.com";
        String emailType = "INVITATION";
        String key = "email:rate_limit:" + recipientEmail + ":" + emailType;

        when(redisTemplate.hasKey(key)).thenReturn(true);

        // Act
        boolean result = emailRateLimitService.isRateLimited(recipientEmail, emailType);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testIsRateLimited_WhenKeyDoesNotExist_ReturnsFalse() {
        // Arrange
        String recipientEmail = "test@example.com";
        String emailType = "INVITATION";
        String key = "email:rate_limit:" + recipientEmail + ":" + emailType;

        when(redisTemplate.hasKey(key)).thenReturn(false);

        // Act
        boolean result = emailRateLimitService.isRateLimited(recipientEmail, emailType);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testSetRateLimit_SetsKeyWithCorrectExpiration() {
        // Arrange
        String recipientEmail = "test@example.com";
        String emailType = "INVITATION";
        String key = "email:rate_limit:" + recipientEmail + ":" + emailType;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        emailRateLimitService.setRateLimit(recipientEmail, emailType);

        // Assert
        verify(valueOperations).set(key, "1", 60, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    void testSetRateLimit_WithDifferentEmailTypes_ShouldSetDifferentKeys() {
        // Arrange
        String recipientEmail = "test@example.com";
        String emailType1 = "INVITATION";
        String emailType2 = "NOTIFICATION";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        emailRateLimitService.setRateLimit(recipientEmail, emailType1);
        emailRateLimitService.setRateLimit(recipientEmail, emailType2);

        // Assert
        verify(valueOperations).set("email:rate_limit:" + recipientEmail + ":" + emailType1, "1",
            60, java.util.concurrent.TimeUnit.SECONDS);
        verify(valueOperations).set("email:rate_limit:" + recipientEmail + ":" + emailType2, "1",
            60, java.util.concurrent.TimeUnit.SECONDS);
    }
}
