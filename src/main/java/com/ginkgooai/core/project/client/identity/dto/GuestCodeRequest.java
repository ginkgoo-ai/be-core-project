package com.ginkgooai.core.project.client.identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuestCodeRequest {
    private String resourceId;
    
    private String ownerEmail;
    
    private String guestEmail;
    
    private String redirectUrl;
    
    private int expiryHours;
}