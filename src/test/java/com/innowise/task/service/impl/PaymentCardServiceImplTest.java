package com.innowise.task.service.impl;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.entity.User;
import com.innowise.task.exception.BusinessRuleException;
import com.innowise.task.exception.NotFoundException;
import com.innowise.task.exception.ValidationException;
import com.innowise.task.mapper.PaymentCardMapper;
import com.innowise.task.repository.PaymentCardRepository;
import com.innowise.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

    @InjectMocks
    private PaymentCardServiceImpl service;

    @Mock
    private PaymentCardRepository cardRepository;
    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private UserRepository userRepository;

    private PaymentCardDTO dto;
    private PaymentCard card;
    private User user;

    @BeforeEach
    void setUp() {
        dto = new PaymentCardDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setNumber("1234567812345678");
        dto.setHolder("John Doe");
        dto.setActive(true);

        user = new User();
        user.setId(1L);

        card = new PaymentCard();
        card.setId(1L);
        card.setUser(user);
        card.setNumber(dto.getNumber());
        card.setHolder(dto.getHolder());
        card.setActive(true);

        lenient().when(paymentCardMapper.toDTO(any(PaymentCard.class))).thenReturn(dto);
        lenient().when(paymentCardMapper.toEntity(any(PaymentCardDTO.class))).thenReturn(card);
    }

    @Test
    void create_shouldSaveCard_whenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUserId(1L)).thenReturn(List.of());
        when(cardRepository.save(any())).thenReturn(card);

        PaymentCardDTO result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(cardRepository).save(any(PaymentCard.class));
    }

    @Test
    void create_shouldThrow_whenDtoNull() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldThrow_whenUserHasFiveCards() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUserId(1L))
                .thenReturn(List.of(new PaymentCard(), new PaymentCard(),
                        new PaymentCard(), new PaymentCard(), new PaymentCard()));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void getById_shouldReturnCard_whenExists() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        PaymentCardDTO result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.getById(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldUpdateAndReturnCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        PaymentCardDTO result = service.update(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void update_shouldThrow_whenNotUpdated() {
        assertThatThrownBy(() -> service.update(1L, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void setActiveStatus_shouldUpdate() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        service.setActiveStatus(1L, true);

        assertThat(card.getActive()).isTrue();
    }

    @Test
    void setActiveStatus_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.setActiveStatus(null, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void delete_shouldDeleteCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        PaymentCardDTO result = service.delete(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void delete_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void findAll_shouldReturnPageOfPaymentDTO() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);

        Page<PaymentCardDTO> result = service.findAll("John", "Doe", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

}
