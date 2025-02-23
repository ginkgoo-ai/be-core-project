package com.ginkgooai.core.project.client.messaging;

import com.ginkgooai.core.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "messaging-service", url="${core-messaging-uri}", configuration = FeignConfig.class)
public interface EmailClient {
    @PostMapping("/emails")
    void sendEmail();
}

