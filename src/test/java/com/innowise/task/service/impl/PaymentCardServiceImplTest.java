package com.innowise.task.service.impl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.entity.User;
import com.innowise.task.exception.BusinessRuleException;
import com.innowise.task.exception.NotFoundException;
import com.innowise.task.exception.ValidationException;
import com.innowise.task.repository.PaymentCardRepository;
import com.innowise.task.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

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
    private UserRepository userRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

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
        when(cardRepository.updateCardById(1L, dto.getNumber(), dto.getHolder()))
                .thenReturn(1);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        PaymentCardDTO result = service.update(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void update_shouldThrow_whenNotUpdated() {
        when(cardRepository.updateCardById(any(), any(), any()))
                .thenReturn(0);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldUpdateAndEvictCache() {
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cardRepository.setActiveStatus(1L, true)).thenReturn(1);
        when(cardRepository.getById(1L)).thenReturn(card);

        service.setActiveStatus(1L, true);

        verify(cache, times(2)).evict(1L);
    }

    @Test
    void delete_shouldDeleteAndEvictCache() {
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        service.delete(1L);

        verify(cardRepository).deleteById(1L);
        verify(cache, times(2)).evict(1L);
    }

    @Test
    void getAllByUserId_shouldReturnCards() {
        when(cardRepository.findAllByUserId(1L)).thenReturn(List.of(card));

        List<PaymentCardDTO> result = service.getAllByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_shouldReturnPageOfPaymentDTO() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Specification<PaymentCard> spec = (root, query, cb) -> cb.conjunction();

        Page<PaymentCard> userPage = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findAll(spec, pageable)).thenReturn(userPage);

        Page<PaymentCardDTO> result = service.findAll(spec, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(cardRepository).findAll(spec, pageable);
    }

    @Test
    void update_shouldThrow_whenDtoNull() {
        assertThatThrownBy(() -> service.update(1L, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage(PaymentCardServiceImpl.CARD_DTO_MUST_NOT_BE_NULL);
    }

    @Test
    void setActiveStatus_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.setActiveStatus(null, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage(PaymentCardServiceImpl.CARD_ID_MUST_NOT_BE_NULL);
    }

    @Test
    void delete_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage(PaymentCardServiceImpl.CARD_ID_MUST_NOT_BE_NULL);
    }
}

