package rca.ac.rw.template.meter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import rca.ac.rw.template.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MeterSpecifications {

    public static Specification<Meter> filterMeters(String meterNumber, UUID userId, boolean includeDeleted) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(meterNumber)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("meterNumber")), "%" + meterNumber.toLowerCase() + "%"));
            }

            if (userId != null) {
                Join<Meter, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            if (!includeDeleted) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}