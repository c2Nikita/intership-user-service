package com.innowise.task.service;


import com.innowise.task.dto.UserDTO;
import com.innowise.task.exception.ServiceException;


public interface UserService extends BaseService<UserDTO>{

    UserDTO updateNameAndSurname(Long id, String name, String surname) throws ServiceException;

}
