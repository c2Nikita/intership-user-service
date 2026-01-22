package com.innowise.task.service.impl;

import com.innowise.task.dto.UserDTO;
import com.innowise.task.entity.User;
import com.innowise.task.exception.ServiceException;
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
    public UserDTO create(UserDTO userDTO) throws ServiceException {
        if (userDTO == null) {
            throw new ServiceException(USER_DTO_MUST_NOT_BE_NULL);
        }

        User user = UserMapper.INSTANCE.toEntity(userDTO);
        User savedUser = userRepository.save(user);

        return UserMapper.INSTANCE.toDto(savedUser);
    }

    @Override
    public UserDTO getById(Long id) throws ServiceException {
        return userRepository.findById(id)
                .map(UserMapper.INSTANCE::toDto)
                .orElseThrow(() -> new ServiceException(USER_NOT_FOUND + id));
    }

    @Override
    public Page<UserDTO> findAll(Specification<?> specification, Pageable pageable) {
        Specification<User> spec = (Specification<User>) specification;

        return userRepository.findAll(spec, pageable)
                .map(UserMapper.INSTANCE::toDto);
    }

    @Transactional
    @Override
    public void setActiveStatus(Long id, boolean active) throws ServiceException {
        int updated = userRepository.setActiveStatus(id, active);
        if (updated == 0) {
            throw new ServiceException(USER_NOT_UPDATED + id);
        }
    }

    @Transactional
    @Override
    public void delete(UserDTO userDTO) throws ServiceException {
        if (userDTO == null || userDTO.getId() == null) {
            throw new ServiceException(USER_ID_MUST_NOT_BE_NULL);
        }

        boolean exists = userRepository.existsById(userDTO.getId());
        if (!exists) {
            throw new ServiceException(USER_NOT_FOUND + userDTO.getId());
        }

        userRepository.deleteById(userDTO.getId());
    }

    @Transactional
    @Override
    public UserDTO updateNameAndSurname(Long id, String name, String surname) throws ServiceException {
        int updated = userRepository.updateNameAndSurnameById(id, name, surname);
        if (updated == 0) {
            throw new ServiceException(USER_NOT_UPDATED + id);
        }

        return getById(id);
    }
}


