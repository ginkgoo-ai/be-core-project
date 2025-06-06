package com.ginkgooai.core.project.config.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ROLE_USER') or (hasRole('ROLE_TALENT') and (hasAuthority('application:' + #applicationId + ':read') or hasAuthority('application:' + #applicationId + ':write')))")
public @interface RequireApplicationReadScope {
}