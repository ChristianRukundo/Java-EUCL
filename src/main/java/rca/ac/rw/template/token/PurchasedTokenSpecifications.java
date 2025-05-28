package rca.ac.rw.template.token;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import rca.ac.rw.template.meter.Meter;

import java.util.ArrayList;
import java.util.List;

public class PurchasedTokenSpecifications {

    public static Specification<PurchasedToken> findByMeter(Meter meter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (meter != null) {

                predicates.add(criteriaBuilder.equal(root.get("meter"), meter));
            }
//            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


}