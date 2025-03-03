package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.project.domain.application.ApplicationStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ApplicationStateMachine {

    private final Map<ApplicationStatus, Set<ApplicationStatus>> allowedTransitions;

    public ApplicationStateMachine() {
        allowedTransitions = new EnumMap<>(ApplicationStatus.class);
        
        // Initialize allowed transitions
        allowedTransitions.put(ApplicationStatus.ADDED, 
            Set.of(ApplicationStatus.REQUESTED));
            
        allowedTransitions.put(ApplicationStatus.REQUESTED, 
            Set.of(ApplicationStatus.DECLINED, ApplicationStatus.SUBMITTED));
            
        allowedTransitions.put(ApplicationStatus.DECLINED, 
            Set.of(ApplicationStatus.REQUESTED));
            
        allowedTransitions.put(ApplicationStatus.SUBMITTED, 
            Set.of(ApplicationStatus.REVIEWED));
            
        allowedTransitions.put(ApplicationStatus.REVIEWED, 
            Set.of(ApplicationStatus.RETAPE, ApplicationStatus.SHORTLISTED));
            
        allowedTransitions.put(ApplicationStatus.RETAPE, 
            Set.of(ApplicationStatus.SUBMITTED));
            
        // Shortlisted is a final state
        allowedTransitions.put(ApplicationStatus.SHORTLISTED, new HashSet<>());
    }

    public void validateTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        Set<ApplicationStatus> allowed = allowedTransitions.get(currentStatus);
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException(
                String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    public Set<ApplicationStatus> getAllowedTransitions(ApplicationStatus currentStatus) {
        return allowedTransitions.getOrDefault(currentStatus, new HashSet<>());
    }
}