package com.ginkgooai.core.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApplicationRequest {
    
    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Role ID is required")
    private String roleId;

    @Valid
    @NotNull(message = "Talent information is required")
    private TalentInfo talent;

    @Valid
    @NotNull(message = "Agent information is required")
    private AgentInfo agent;

    private Map<String, Object> additionalInfo;

    @Data
    @Builder
    public static class TalentInfo {
        @NotBlank(message = "Talent name is required")
        private String name;

        @NotBlank(message = "Talent email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "At least one profile URL is required")
        private String imdbProfileUrl;

        private String spotlightProfileUrl;

        private Map<String, Object> attributes;
    }

    @Data
    @Builder
    public static class AgentInfo {
        @NotBlank(message = "Agent name is required")
        private String name;

        @NotBlank(message = "Agent email is required")
        @Email(message = "Invalid email format")
        private String email;

        private String agencyName;

        private String phone;
    }
}