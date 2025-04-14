package com.ginkgooai.core.project.repository.specification;

import com.ginkgooai.core.project.domain.application.ShortlistItem;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShortlistItemSpecification {

    public static Specification<ShortlistItem> findAllWithFilters(String shortlistId,
                                                                  String keyword, String roleId, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                                  String talentId) {
        return (root, query, cb) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return cb.and(buildPredicates(root, query, cb, shortlistId, keyword, roleId,
                    startDateTime, endDateTime, talentId).toArray(new Predicate[0]));
            }

            query.distinct(true);

            root.fetch("shortlist", JoinType.LEFT);
            root.fetch("application", JoinType.LEFT).fetch("talent", JoinType.LEFT);
            root.fetch("submissions", JoinType.LEFT);

            return cb.and(buildPredicates(root, query, cb, shortlistId, keyword, roleId,
                startDateTime, endDateTime, talentId).toArray(new Predicate[0]));
        };
    }

    private static List<Predicate> buildPredicates(
        jakarta.persistence.criteria.Root<ShortlistItem> root,
        jakarta.persistence.criteria.CriteriaQuery<?> query,
        jakarta.persistence.criteria.CriteriaBuilder cb, String shortlistId, String keyword,
        String roleId, LocalDateTime startDateTime, LocalDateTime endDateTime, String talentId) {
        List<Predicate> predicates = new ArrayList<>();

        // Join tables
        Join<Object, Object> application = root.join("application", JoinType.LEFT);
        Join<Object, Object> role = application.join("role", JoinType.LEFT);
        Join<Object, Object> talentJoin = application.join("talent", JoinType.LEFT);
        Join<Object, Object> submissions = root.join("submissions", JoinType.LEFT);

        // Add shortlistId condition
        predicates.add(cb.equal(root.get("shortlist").get("id"), shortlistId));

        // Add keyword search conditions
        if (StringUtils.hasText(keyword)) {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(cb.like(cb.lower(role.get("name")), likePattern),
                cb.like(cb.lower(cb.concat(cb.concat(talentJoin.get("firstName"), " "),
                    talentJoin.get("lastName"))), likePattern),
                cb.like(cb.lower(talentJoin.get("email")), likePattern),
                cb.like(cb.lower(submissions.get("videoName")), likePattern)));
        }

        // Add roleId condition
        if (StringUtils.hasText(roleId)) {
            predicates.add(cb.equal(role.get("id"), roleId));
        }


        // Add talent ID filter
        if (StringUtils.hasText(talentId)) {
            predicates.add(cb.equal(root.get("application").get("talent").get("id"), talentId));
        }

        // Add date range conditions
        if (startDateTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
        }
        if (endDateTime != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
        }

        return predicates;
    }
}
