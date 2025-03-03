package com.ginkgooai.core.project.dto.response;

import com.ginkgooai.core.project.domain.project.ProjectNda;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response payload for a project NDA")
public class ProjectNdaResponse {
    @Schema(description = "ID of the NDA", example = "nda123")
    private String id;

    @Schema(description = "Whether NDA is required", example = "true")
    private Boolean requiresNda;

    @Schema(description = "Whether NDA applies to all", example = "true")
    private Boolean applyToAll;

    @Schema(description = "NDA version", example = "v2.1")
    private String version;

    @Schema(description = "Full name of the signer", example = "John Doe")
    private String fullName;

    @Schema(description = "Title of the signer", example = "Producer")
    private String title;

    @Schema(description = "Company of the signer", example = "Ginkgoo Studios")
    private String company;

    @Schema(description = "URL or path to signature file", example = "https://example.com/signature.pdf")
    private String signatureUrl;

    @Schema(description = "Last updated timestamp", example = "2025-02-21T10:00:00")
    private String lastUpdated;

    @Schema(description = "Project ID associated with the NDA", example = "proj123")
    private String projectId;

    public static ProjectNdaResponse mapToProjectNdaResponse(ProjectNda nda) {
        ProjectNdaResponse response = new ProjectNdaResponse();
        response.setId(nda.getId());
        response.setRequiresNda(nda.getRequiresNda());
        response.setApplyToAll(nda.getApplyToAll());
        response.setVersion(nda.getVersion());
        response.setFullName(nda.getFullName());
        response.setTitle(nda.getTitle());
        response.setCompany(nda.getCompany());
        response.setSignatureUrl(nda.getSignatureUrl());
        response.setLastUpdated(nda.getUpdatedAt().toString());
        response.setProjectId(nda.getProject().getId());
        return response;
    }


}