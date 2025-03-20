package com.ginkgooai.core.project.client.identity.dto;

import lombok.Data;

import java.util.*;

@Data
public class UserInfo {
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

