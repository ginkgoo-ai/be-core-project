package com.ginkgooai.core.project.config.jpa;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

import com.ginkgooai.core.common.utils.ContextUtils;

public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(ContextUtils.getUserId()).or(() -> Optional.of("system"));
    }
}