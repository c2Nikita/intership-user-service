package com.innowise.task.service;

import com.innowise.task.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface BaseService <T> {

    T create(T dto);
    T getById(Long id);
    Page<T> findAll(String name, String surname, Pageable pageable);
    T setActiveStatus(Long id, boolean active);
    T delete(Long id);
}
