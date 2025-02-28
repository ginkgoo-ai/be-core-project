package com.ginkgooai.core.project.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    DRAFTING("Drafting"),
    IN_PROGRESS("In Progress"),
    COMPLETE("Complete"),
    ARCHIVED("Archived");

    private final String value;
    
    ProjectStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}