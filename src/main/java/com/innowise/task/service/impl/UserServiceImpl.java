package com.innowise.task.service.impl;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.entity.User;
import com.innowise.task.exception.NotFoundException;
import com.innowise.task.exception.ServiceException;
import com.innowise.task.exception.ValidationException;
import com.innowise.task.mapper.UserMapper;
import com.innowise.task.repository.UserRepository;
import com.innowise.task.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    public static final String USER_NOT_FOUND = "User not found with id ";
    public static final String USER_NOT_UPDATED = "User not found or not updated with id ";
    public static final String USER_ID_MUST_NOT_BE_NULL = "User id must not be null";
    public static final String USER_DTO_MUST_NOT_BE_NULL = "User DTO must not be null";

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public UserDTO create(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ValidationException(USER_DTO_MUST_NOT_BE_NULL);
        }

        User user = UserMapper.INSTANCE.toEntity(userDTO);
        User savedUser = userRepository.save(user);

        return UserMapper.INSTANCE.toDto(savedUser);
    }

    @Override
    public UserDTO getById(Long id) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }
        return userRepository.findById(id)
                .map(UserMapper.INSTANCE::toDto)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    @Override
    public Page<UserDTO> findAll(Specification<?> specification, Pageable pageable) {
        Specification<User> spec = (Specification<User>) specification;

        return userRepository.findAll(spec, pageable)
                .map(UserMapper.INSTANCE::toDto);
    }

    @Transactional
    @Override
    public void setActiveStatus(Long id, boolean active) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        int updated = userRepository.setActiveStatus(id, active);
        if (updated == 0) {
            throw new NotFoundException(USER_NOT_UPDATED + id);
        }
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_NOT_FOUND + id);
        }

        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public UserDTO updateNameAndSurname(Long id, String name, String surname) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        int updated = userRepository.updateNameAndSurnameById(id, name, surname);
        if (updated == 0) {
            throw new NotFoundException(USER_NOT_UPDATED + id);
        }

        return getById(id);
    }
}


