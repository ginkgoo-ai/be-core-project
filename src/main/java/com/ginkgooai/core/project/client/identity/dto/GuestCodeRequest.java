package com.ginkgooai.core.project.client.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestCodeRequest implements Serializable {
   
    private static final long serialVersionUID = 1L;
    
    private String workspaceId;
    
    private String resource;
    
    private String resourceId;

    private String guestName;

    private String guestEmail;
    
    private boolean write;
    
    private String redirectUrl;
    
    private int expiryHours;
}