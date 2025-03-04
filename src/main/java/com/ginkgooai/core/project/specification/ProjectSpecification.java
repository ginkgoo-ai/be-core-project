package com.ginkgooai.core.project.specification;

import com.ginkgooai.core.project.domain.project.Project;
import com.ginkgooai.core.project.domain.project.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecification {
    
    public static Specification<Project> hasWorkspaceId(String workspaceId) {
        return (root, query, criteriaBuilder) -> {
            if (workspaceId == null || workspaceId.trim().isEmpty()) {
                return null; // No filtering if workspaceId is null or empty
            }
            return criteriaBuilder.equal(root.get("workspaceId"), workspaceId);
        };
    } 

    public static Specification<Project> hasNameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return null; // No filtering if name is null or empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null; // No filtering if status is null
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    // Add more specifications as needed (e.g., for ownerId, updatedAt, etc.)
    public static Specification<Project> hasOwnerId(String ownerId) {
        return (root, query, criteriaBuilder) -> {
            if (ownerId == null || ownerId.trim().isEmpty()) {
                return null; // No filtering if ownerId is null or empty
            }
            return criteriaBuilder.equal(root.get("ownerId"), ownerId);
        };
    }

}