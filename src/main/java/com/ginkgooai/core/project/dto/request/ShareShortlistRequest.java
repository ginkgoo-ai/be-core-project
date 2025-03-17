package com.ginkgooai.core.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShareShortlistRequest {
    @NotEmpty
    private List<String> submissionIds;
    
    @NotBlank
    @Email
    private String recipientEmail;
    
    private Integer expiresInDays;
}