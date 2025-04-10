package com.ginkgooai.core.project.domain.application;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    ADDED("Added", "Initial state when application is created"),
    NDA_SENT("NDA Sent", "NDA sent to talent/agent"),
    NDA_SIGNED("NDA Signed", "NDA signed by talent/agent"),
    REQUESTED("Requested", "Invitation sent to talent/agent"),
    DECLINED("Declined", "Invitation was declined"),
    SUBMITTED("Submitted", "Video has been uploaded"),
    REVIEWED("Reviewed", "Application has been reviewed"),
    RETAPE("Retape", "New video submission requested"),
    SHORTLISTED("Shortlisted", "Candidate added to shortlist"),
    CAST("Cast", "Candidate selected for the role"),
    ;

    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}