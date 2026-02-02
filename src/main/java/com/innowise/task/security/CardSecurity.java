package com.innowise.task.security;

import com.innowise.task.service.PaymentCardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("cardSecurity")
public class CardSecurity {

    private final PaymentCardService paymentCardService;

    public CardSecurity(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    public boolean isOwner(Long cardId) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        Long currentUserId;
        try {
            currentUserId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            return false;
        }

        Long ownerId = paymentCardService.getOwnerId(cardId);
        return currentUserId.equals(ownerId);
    }
}
