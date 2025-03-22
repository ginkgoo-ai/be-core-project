package com.ginkgooai.core.project.client.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class UserInfoResponse {
    private String id;
    
    private String sub;
    
    private String email;

    private String name;
    
    private String firstName;
    
    private String lastName;
    
    private boolean enabled;
    
    private Set<String> roles;

    private String picture;

}

