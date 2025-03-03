package com.ginkgooai.core.project.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
public class TalentUpdateRequest {
    private String name;
    private String email;
    private String imdbProfileUrl;
    private String spotlightProfileUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;
    private String ethnicBackground;
    private Integer heightCm;
    private Integer weightKg;
    private String bodyType;
    private String hairColor;
    private String eyeColor;
    private Set<String> skills;
    private Set<String> languages;
    private String unionMembership;
    private String personalWebsite;
    private Map<String, String> socialMediaLinks;
    private Map<String, Object> attributes;  // 扩展属性
}