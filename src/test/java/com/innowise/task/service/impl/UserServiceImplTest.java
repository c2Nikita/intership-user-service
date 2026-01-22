package com.innowise.task.service.impl;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.entity.User;
import com.innowise.task.exception.ServiceException;
import com.innowise.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl service;

    @Mock
    private UserRepository repository;

    private UserDTO dto;
    private User entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dto = new UserDTO();
        dto.setId(1L);
        dto.setName("John");
        dto.setSurname("Doe");
        dto.setBirthDate(LocalDate.of(2000,1,1));
        dto.setEmail("john@example.com");
        dto.setActive(true);

        entity = new User();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setSurname(dto.getSurname());
        entity.setBirthDate(dto.getBirthDate());
        entity.setEmail(dto.getEmail());
        entity.setActive(dto.getActive());
    }

    @Test
    void create_shouldSaveUser_whenValid() throws ServiceException {
        when(repository.save(any(User.class))).thenReturn(entity);

        UserDTO result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dto.getId());
        verify(repository).save(any(User.class));
    }

    @Test
    void getById_shouldReturnUser_whenExists() throws ServiceException {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        UserDTO result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ServiceException ex = catchThrowableOfType(() -> service.getById(1L), ServiceException.class);
        assertThat(ex.getMessage()).isEqualTo("User not found with id 1");
    }

    @Test
    void updateNameAndSurname_shouldCallRepositoryAndReturnUpdated() throws ServiceException {
        when(repository.updateNameAndSurnameById(1L, "New", "Name")).thenReturn(1);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        UserDTO result = service.updateNameAndSurname(1L, "New", "Name");

        assertThat(result).isNotNull();
        verify(repository).updateNameAndSurnameById(1L, "New", "Name");
    }

    @Test
    void delete_shouldCallRepository_whenExists() throws ServiceException {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        service.delete(dto);

        verify(repository).deleteById(1L);
    }
}
