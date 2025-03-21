package com.ginkgooai.core.project.client.identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuestCodeRequest {
    private String resource;
    
    private String resourceId;

    private String guestName;

    private String guestEmail;
    
    private boolean write;
    
    private String redirectUrl;
    
    private int expiryHours;
}