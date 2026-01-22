package com.innowise.task.service;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.exception.ServiceException;

import java.util.List;

public interface PaymentCardService extends BaseService<PaymentCardDTO>{
    List<PaymentCardDTO> getAllByUserId(Long userId) throws ServiceException;

    PaymentCardDTO update(Long id, PaymentCardDTO dto) throws ServiceException;

}
