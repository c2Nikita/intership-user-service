package com.innowise.task.service.impl;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.entity.User;
import com.innowise.task.exception.NotFoundException;
import com.innowise.task.exception.ValidationException;
import com.innowise.task.mapper.UserMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl service;

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper userMapper;

    private UserDTO dto;
    private User user;

    @BeforeEach
    void setUp() {
        dto = new UserDTO();
        dto.setId(1L);
        dto.setName("John");
        dto.setSurname("Doe");
        dto.setActive(true);

        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");
        user.setActive(true);

        lenient().when(userMapper.toEntity(any(UserDTO.class))).thenReturn(user);
        lenient().when(userMapper.toDto(any(User.class))).thenReturn(dto);
    }

    @Test
    void create_shouldSaveUser() {
        when(repository.save(any(User.class))).thenReturn(user);

        UserDTO result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getSurname()).isEqualTo("Doe");

        verify(repository).save(user);
    }

    @Test
    void create_shouldThrow_whenDtoNull() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_shouldReturnUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = service.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        verify(repository).findById(1L);
    }

    @Test
    void getById_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.getById(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateNameAndSurname_shouldUpdate() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = service.updateNameAndSurname(1L, "New", "Name");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("New");
        assertThat(user.getSurname()).isEqualTo("Name");

        verify(repository).findById(1L);
    }

    @Test
    void updateNameAndSurname_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.updateNameAndSurname(null, "A", "B"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateNameAndSurname_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateNameAndSurname(1L, "A", "B"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActiveStatus_shouldUpdate() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = service.setActiveStatus(1L, false);

        assertThat(result).isNotNull();
        assertThat(user.getActive()).isFalse();

        verify(repository).findById(1L);
    }

    @Test
    void setActiveStatus_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.setActiveStatus(null, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void setActiveStatus_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setActiveStatus(1L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldDeleteUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = service.delete(1L);

        assertThat(result).isNotNull();
        verify(repository).delete(user);
    }

    @Test
    void delete_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPageOfUserDTO() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);

        Page<UserDTO> result = service.findAll("John", "Doe", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }
}
