package com.innowise.task.controller;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.service.UserService;
import com.innowise.task.specification.UserSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }


    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(userDTO));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserDTO> setActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        userService.setActiveStatus(id, active);

        return ResponseEntity.ok(userService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateNameAndSurname(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String surname) {
        UserDTO updatedUser = userService.updateNameAndSurname(id, name, surname);

        return ResponseEntity.ok(updatedUser);

    }


    @GetMapping()
    public ResponseEntity<Page<UserDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Specification specification = Specification.where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));
        Page<UserDTO> users = userService.findAll(specification, pageable);

        return ResponseEntity.ok(users);

    }


}
