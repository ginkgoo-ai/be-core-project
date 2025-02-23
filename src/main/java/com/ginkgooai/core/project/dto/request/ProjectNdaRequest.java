package com.ginkgooai.core.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating or updating a project NDA")
public class ProjectNdaRequest {
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
}