package com.innowise.task.specification;

import com.innowise.task.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {
    public static Specification<PaymentCard> userHasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.join("user").get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PaymentCard> userHasSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.join("user").get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }
}
