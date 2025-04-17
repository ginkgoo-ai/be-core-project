package com.ginkgooai.core.project.domain.role;

import lombok.Getter;
import lombok.Setter;

public enum RoleStatus {
    DRAFTING("Drafting"),
    CASTING("Casting"),
	SUBMITTED("Submitted"),
    SHORTLISTED("Shortlisted"),
    CAST("Cast");

    @Getter
    @Setter
    private final String value;
    
    RoleStatus(String value) {
        this.value = value;
    }
}