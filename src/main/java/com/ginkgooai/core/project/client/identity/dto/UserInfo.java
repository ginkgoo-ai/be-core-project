package com.ginkgooai.core.project.client.identity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class UserInfo {
    private String id;
    
    private String sub;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private boolean enabled;
    
    private Set<String> roles;
}

