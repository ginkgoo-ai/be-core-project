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
public class GuestCodeResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String guestCode;

    private String resourceId;

    private String expiresAt;

    private int expiryHours;
}