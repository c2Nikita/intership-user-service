package com.innowise.task.service;

import com.innowise.task.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface BaseService <T> {

    T create(T dto) throws ServiceException;
    T getById(Long id) throws ServiceException;
    Page<T> findAll(Specification<?> specification, Pageable pageable) throws ServiceException;
    void setActiveStatus(Long id, boolean active) throws ServiceException;
    void delete(T dto) throws ServiceException;
}
