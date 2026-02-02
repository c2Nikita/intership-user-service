package com.innowise.task.repository;

import com.innowise.task.entity.PaymentCard;
import com.innowise.task.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaymentCardRepositoryTest {

    @Autowired
    private PaymentCardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private PaymentCard card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Nikita");
        user.setSurname("Velich");
        user.setEmail("nikita@example.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setActive(true);
        user = userRepository.save(user);

        card = new PaymentCard();
        card.setUser(user);
        card.setNumber("1234567890123456");
        card.setHolder("Nikita Velich");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);
        card = cardRepository.save(card);
    }

    @Test
    void testSaveAndFindById() {
        Optional<PaymentCard> found = cardRepository.findById(card.getId());

        assertAll(
                () -> assertTrue(found.isPresent(), "Card should be present"),
                () -> assertEquals("1234567890123456", found.get().getNumber(), "Card number should match")
        );
    }

    @Test
    void testUpdateCardById() {
        int updated = cardRepository.updateCardById(card.getId(), "6543210987654321", "Ivan Petrov");
        Optional<PaymentCard> updatedCard = cardRepository.findById(card.getId());

        assertAll(
                () -> assertEquals(1, updated, "Update count should be 1"),
                () -> assertTrue(updatedCard.isPresent(), "Updated card should exist"),
                () -> assertEquals("6543210987654321", updatedCard.get().getNumber(), "Card number should be updated"),
                () -> assertEquals("Ivan Petrov", updatedCard.get().getHolder(), "Card holder should be updated")
        );
    }

    @Test
    void testSetActiveStatusNative() {
        int updated = cardRepository.setActiveStatusNative(card.getId(), false);
        Optional<PaymentCard> updatedCard = cardRepository.findById(card.getId());

        assertAll(
                () -> assertEquals(1, updated, "Update count should be 1"),
                () -> assertTrue(updatedCard.isPresent(), "Updated card should exist"),
                () -> assertFalse(updatedCard.get().getActive(), "Card should be inactive")
        );
    }

    @Test
    void testFindAllByUserId() {
        List<PaymentCard> cards = cardRepository.findAllByUserId(user.getId());

        assertAll(
                () -> assertEquals(1, cards.size(), "User should have 1 card"),
                () -> assertEquals(card.getId(), cards.get(0).getId(), "Card ID should match")
        );
    }

    @Test
    void testFindAllWithSpecificationAndPageable() {
        Specification<PaymentCard> spec = (root, query, cb) -> cb.equal(root.get("active"), true);
        Page<PaymentCard> page = cardRepository.findAll(spec, PageRequest.of(0, 10));

        assertAll(
                () -> assertEquals(1, page.getTotalElements(), "Should find 1 active card"),
                () -> assertEquals(card.getId(), page.getContent().get(0).getId(), "Card ID should match")
        );
    }
}
