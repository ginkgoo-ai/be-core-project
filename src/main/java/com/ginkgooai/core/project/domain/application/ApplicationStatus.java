package com.ginkgooai.core.project.domain.application;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    ADDED("Added", "Initial state when application is created"),
    REQUESTED("Requested", "Invitation sent to talent/agent"),
    DECLINED("Declined", "Invitation was declined"),
    SUBMITTED("Submitted", "Video has been uploaded"),
    REVIEWED("Reviewed", "Application has been reviewed"),
    RETAPE("Retape", "New video submission requested"),
    SHORTLISTED("Shortlisted", "Candidate added to shortlist");

    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}