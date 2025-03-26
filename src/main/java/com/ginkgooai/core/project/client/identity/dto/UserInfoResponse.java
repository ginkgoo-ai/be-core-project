package com.ginkgooai.core.project.client.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse implements Serializable {

    private static final long serialVersionUID = 835098014158567328L;

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

