package com.ginkgooai.core.project.service.rate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailRateLimitService {

    private final RateLimitService rateLimitService;
    private static final String EMAIL_RATE_LIMIT_KEY_FORMAT = "email:%s:%s";

    public boolean isRateLimited(String recipientEmail, String emailType) {
        String key = String.format(EMAIL_RATE_LIMIT_KEY_FORMAT, recipientEmail, emailType);
        return rateLimitService.isRateLimited(key);
    }

    public void setRateLimit(String recipientEmail, String emailType) {
        String key = String.format(EMAIL_RATE_LIMIT_KEY_FORMAT, recipientEmail, emailType);
        rateLimitService.setRateLimit(key);
    }

    public long getRemainingTime(String recipientEmail, String emailType) {
        String key = String.format(EMAIL_RATE_LIMIT_KEY_FORMAT, recipientEmail, emailType);
        return rateLimitService.getRemainingTime(key);
    }
}
