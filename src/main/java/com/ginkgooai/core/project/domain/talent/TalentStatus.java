package com.ginkgooai.core.project.domain.talent;

import lombok.Getter;

@Getter
public enum TalentStatus {
    
    DRAFT("Draft", "Initial state when talent profile is created but incomplete", false),
    
    PENDING_VERIFICATION("Pending Verification", "Profile is complete but needs verification", false),
    
    ACTIVE("Active", "Talent is verified and available for casting", true),
    
    UNAVAILABLE("Unavailable", "Temporarily not available for new roles", false),
    
    EXCLUSIVE("Exclusive", "Under exclusive contract", true),
    
    ARCHIVED("Archived", "No longer active in the system", false),
    
    BLACKLISTED("Blacklisted", "Not eligible for future roles", false);

    private final String displayName;
    private final String description;
    private final boolean availableForCasting;

    TalentStatus(String displayName, String description, boolean availableForCasting) {
        this.displayName = displayName;
        this.description = description;
        this.availableForCasting = availableForCasting;
    }
}