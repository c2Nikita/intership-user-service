package com.innowise.task.controller;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.service.PaymentCardService;
import com.innowise.task.specification.PaymentCardSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isOwner(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getCard(@PathVariable Long id) {
        PaymentCardDTO paymentCardDTO = paymentCardService.getById(id);
        return ResponseEntity.ok(paymentCardDTO);
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> deleteCard(@PathVariable Long id) {
        PaymentCardDTO deletedCard = paymentCardService.delete(id);

        return ResponseEntity.ok(deletedCard);
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isOwner(#id)")
    @PatchMapping("/{id}/active")
    public ResponseEntity<PaymentCardDTO> setActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        PaymentCardDTO paymentCardDTO = paymentCardService.setActiveStatus(id, active);

        return ResponseEntity.ok(paymentCardDTO);
    }

    @PreAuthorize("hasRole('ADMIN') or #paymentCardDTO.userId == authentication.principal")
    @PostMapping
    public ResponseEntity<PaymentCardDTO> createCard(@RequestBody @Valid PaymentCardDTO paymentCardDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.create(paymentCardDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PaymentCardDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
            ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<PaymentCardDTO> cards = paymentCardService.findAll(name, surname, pageable);

        return ResponseEntity.ok(cards);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    @GetMapping("/user/{userId}")
    public  ResponseEntity<List<PaymentCardDTO>> getAllByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> update(@PathVariable Long id, @RequestBody @Valid PaymentCardDTO dto) {
        return ResponseEntity.ok(paymentCardService.update(id, dto));
    }

}
