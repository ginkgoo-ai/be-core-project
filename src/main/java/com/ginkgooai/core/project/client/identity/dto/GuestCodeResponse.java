package com.ginkgooai.core.project.client.identity.dto;

import lombok.Data;

@Data
public class GuestCodeResponse {
    private String guestCode;

    private String resourceId;

    private String expiresAt;

    private int expiryHours;
}