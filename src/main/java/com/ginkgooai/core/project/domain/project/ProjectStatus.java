package com.ginkgooai.core.project.domain.project;

import lombok.Getter;

public enum ProjectStatus {
    DRAFTING("Drafting"),
    IN_PROGRESS("In Progress"),
    COMPLETE("Complete"),
    ARCHIVED("Archived");
  
    @Getter
    private final String value;
    
    ProjectStatus(String value) {
        this.value = value;
    }
    
}