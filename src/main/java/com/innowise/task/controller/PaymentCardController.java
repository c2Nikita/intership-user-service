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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> getCard(@PathVariable Long id) {
        PaymentCardDTO paymentCardDTO = paymentCardService.getById(id);
        return ResponseEntity.ok(paymentCardDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<PaymentCardDTO> setActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        paymentCardService.setActiveStatus(id, active);

        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentCardDTO> createCard(@RequestBody @Valid PaymentCardDTO paymentCardDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.create(paymentCardDTO));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
            ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Specification specification = Specification.where(PaymentCardSpecification.userHasName(name))
                        .and(PaymentCardSpecification.userHasSurname(surname));
        Page<PaymentCardDTO> cards = paymentCardService.findAll(specification, pageable);

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public  ResponseEntity<List<PaymentCardDTO>> getAllByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDTO> update(@PathVariable Long id, @RequestBody @Valid PaymentCardDTO dto) {
        return ResponseEntity.ok(paymentCardService.update(id, dto));
    }

}
