package com.innowise.task.repository;

import com.innowise.task.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Nikita");
        user.setSurname("Velich");
        user.setEmail("nikita@example.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setActive(true);
        user = userRepository.save(user);
    }

    @Test
    void testSaveAndFindById() {
        Optional<User> found = userRepository.findById(user.getId());
        assertAll(
                () -> assertTrue(found.isPresent(), "User should be present"),
                () -> assertEquals("nikita@example.com", found.get().getEmail(), "Email should match")
        );
    }

    @Test
    void testUpdateNameAndSurnameById() {
        int updated = userRepository.updateNameAndSurnameById(user.getId(), "Ivan", "Petrov");
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(1, updated, "Update count should be 1"),
                () -> assertEquals("Ivan", updatedUser.getName(), "Name should be updated"),
                () -> assertEquals("Petrov", updatedUser.getSurname(), "Surname should be updated")
        );
    }

    @Test
    void testSetActiveStatus() {
        int updated = userRepository.setActiveStatus(user.getId(), false);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(1, updated, "Update count should be 1"),
                () -> assertFalse(updatedUser.getActive(), "User should be inactive")
        );
    }

    @Test
    void testFindAllWithSpecificationAndPageable() {
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("active"), true);
        Page<User> page = userRepository.findAll(spec, PageRequest.of(0, 10));

        assertAll(
                () -> assertEquals(1, page.getTotalElements(), "Should find 1 active user"),
                () -> assertEquals(user.getId(), page.getContent().get(0).getId(), "User ID should match")
        );
    }
}
