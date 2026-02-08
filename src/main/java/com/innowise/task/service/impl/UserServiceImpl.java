package com.innowise.task.service.impl;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.entity.User;
import com.innowise.task.exception.NotFoundException;
import com.innowise.task.exception.ValidationException;
import com.innowise.task.mapper.UserMapper;
import com.innowise.task.repository.UserRepository;
import com.innowise.task.service.UserService;
import com.innowise.task.specification.UserSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "User not found with id ";
    private static final String USER_ID_MUST_NOT_BE_NULL = "User id must not be null";
    private static final String USER_DTO_MUST_NOT_BE_NULL = "User DTO must not be null";

    private UserRepository userRepository;

    private UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @CachePut(value = "users", key = "#result.id")
    @Transactional
    @Override
    public UserDTO create(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ValidationException(USER_DTO_MUST_NOT_BE_NULL);
        }

        User user = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }
    @Cacheable(value = "users", key = "#id")
    @Override
    public UserDTO getById(Long id) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
    }

    @Override
    public Page<UserDTO> findAll(String name, String surname, Pageable pageable) {
        Specification<User> specification = Specification.where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        return userRepository.findAll(specification, pageable)
                .map(userMapper::toDto);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    @Override
    public UserDTO setActiveStatus(Long id, boolean active) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
        user.setActive(active);

        return userMapper.toDto(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "cardsByUserId", key = "#id")
    })
    @Transactional
    @Override
    public UserDTO delete(Long id) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));
        userRepository.delete(user);

        return userMapper.toDto(user);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    @Override
    public UserDTO updateNameAndSurname(Long id, String name, String surname) {
        if (id == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + id));

        user.setName(name);
        user.setSurname(surname);

        return userMapper.toDto(user);
    }
}


