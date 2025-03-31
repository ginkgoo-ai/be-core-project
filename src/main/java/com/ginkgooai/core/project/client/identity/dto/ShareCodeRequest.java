package com.ginkgooai.core.project.client.identity.dto;

import com.ginkgooai.core.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareCodeRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String workspaceId;

	private String resource;

	private String resourceId;

	private String guestName;

	private String guestEmail;

	private List<Role> roles;

	private boolean write;

	private int expiryHours;
}