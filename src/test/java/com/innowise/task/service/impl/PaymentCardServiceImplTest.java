package com.innowise.task.service.impl;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.exception.ServiceException;
import com.innowise.task.repository.PaymentCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import com.innowise.task.entity.User;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentCardServiceImplTest {

    @InjectMocks
    private PaymentCardServiceImpl service;

    @Mock
    private PaymentCardRepository repository;

    private PaymentCardDTO dto;
    private PaymentCard entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dto = new PaymentCardDTO();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setNumber("1234567812345678");
        dto.setHolder("John Doe");
        dto.setExpirationDate(LocalDate.now().plusYears(1));
        dto.setActive(true);

        User user = new User();
        user.setId(dto.getUserId());

        entity = new PaymentCard();
        entity.setId(dto.getId());
        entity.setUser(user);
        entity.setNumber(dto.getNumber());
        entity.setHolder(dto.getHolder());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setActive(dto.getActive());
    }

    @Test
    void create_shouldSaveCard_whenValid() throws ServiceException {
        when(repository.findAllByUserId(1L)).thenReturn(Collections.emptyList());
        when(repository.save(any(PaymentCard.class))).thenReturn(entity);

        PaymentCardDTO result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dto.getId());
        verify(repository).save(any(PaymentCard.class));
    }

    @Test
    void create_shouldThrow_whenUserHas5Cards() {
        when(repository.findAllByUserId(1L)).thenReturn(Arrays.asList(
                new PaymentCard(), new PaymentCard(), new PaymentCard(),
                new PaymentCard(), new PaymentCard()
        ));

        ServiceException ex = catchThrowableOfType(() -> service.create(dto), ServiceException.class);
        assertThat(ex.getMessage()).isEqualTo("User cannot have more than 5 cards");
    }

    @Test
    void getById_shouldReturnCard_whenExists() throws ServiceException {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        PaymentCardDTO result = service.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entity.getId());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ServiceException ex = catchThrowableOfType(() -> service.getById(1L), ServiceException.class);
        assertThat(ex.getMessage()).isEqualTo("Payment card not found with id 1");
    }

    @Test
    void update_shouldCallRepositoryAndReturnUpdated() throws ServiceException {
        when(repository.updateCardById(1L, dto.getNumber(), dto.getHolder())).thenReturn(1);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        PaymentCardDTO result = service.update(1L, dto);

        assertThat(result.getId()).isEqualTo(dto.getId());
        verify(repository).updateCardById(1L, dto.getNumber(), dto.getHolder());
    }

    @Test
    void delete_shouldCallRepository_whenExists() throws ServiceException {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        service.delete(dto);

        verify(repository).deleteById(1L);
    }
}
