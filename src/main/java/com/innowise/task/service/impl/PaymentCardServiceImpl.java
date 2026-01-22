package com.innowise.task.service.impl;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.exception.ServiceException;
import com.innowise.task.mapper.PaymentCardMapper;
import com.innowise.task.repository.PaymentCardRepository;
import com.innowise.task.service.PaymentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    public static final String CARD_NOT_FOUND = "Payment card not found with id ";
    public static final String CARD_NOT_UPDATED = "Payment card not found or not updated with id ";
    public static final String CARD_ID_MUST_NOT_BE_NULL = "Payment card id must not be null";
    public static final String CARD_DTO_MUST_NOT_BE_NULL = "Payment card DTO must not be null";
    public static final String USER_ID_MUST_NOT_BE_NULL = "User id must not be null";
    public static final String USER_ID_MUST_NOT_HAVE_MORE_THAN_FIVE_CARDS = "User cannot have more than 5 cards";

    @Autowired
    private PaymentCardRepository cardRepository;

    @Transactional
    @Override
    public PaymentCardDTO create(PaymentCardDTO dto) throws ServiceException {
        if (dto == null) {
            throw new ServiceException(CARD_DTO_MUST_NOT_BE_NULL);
        }

        if (dto.getUserId() == null) {
            throw new ServiceException(USER_ID_MUST_NOT_BE_NULL);
        }

        List<PaymentCard> existingCards = cardRepository.findAllByUserId(dto.getUserId());
        if (existingCards.size() >= 5) {
            throw new ServiceException(USER_ID_MUST_NOT_HAVE_MORE_THAN_FIVE_CARDS);
        }

        PaymentCard card = PaymentCardMapper.INSTANCE.toEntity(dto);
        PaymentCard saved = cardRepository.save(card);

        return PaymentCardMapper.INSTANCE.toDTO(saved);
    }

    @Override
    public PaymentCardDTO getById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException(CARD_ID_MUST_NOT_BE_NULL);
        }

        return cardRepository.findById(id)
                .map(PaymentCardMapper.INSTANCE::toDTO)
                .orElseThrow(() -> new ServiceException(CARD_NOT_FOUND + id));
    }

    @Override
    public Page<PaymentCardDTO> findAll(Specification<?> specification, Pageable pageable)
            throws ServiceException {

        Specification<PaymentCard> spec = (Specification<PaymentCard>) specification;

        return cardRepository.findAll(spec, pageable)
                .map(PaymentCardMapper.INSTANCE::toDTO);
    }

    @Transactional
    @Override
    public void setActiveStatus(Long id, boolean active) throws ServiceException {
        if (id == null) {
            throw new ServiceException(CARD_ID_MUST_NOT_BE_NULL);
        }

        int updated = cardRepository.setActiveStatus(id, active);
        if (updated == 0) {
            throw new ServiceException(CARD_NOT_UPDATED + id);
        }
    }

    @Transactional
    @Override
    public void delete(PaymentCardDTO dto) throws ServiceException {
        if (dto == null || dto.getId() == null) {
            throw new ServiceException(CARD_ID_MUST_NOT_BE_NULL);
        }

        boolean exists = cardRepository.existsById(dto.getId());
        if (!exists) {
            throw new ServiceException(CARD_NOT_FOUND + dto.getId());
        }

        cardRepository.deleteById(dto.getId());
    }

    @Override
    public List<PaymentCardDTO> getAllByUserId(Long userId) throws ServiceException {
        if (userId == null) {
            throw new ServiceException(USER_ID_MUST_NOT_BE_NULL);
        }

        return cardRepository.findAllByUserId(userId)
                .stream()
                .map(PaymentCardMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PaymentCardDTO update(Long id, PaymentCardDTO dto) throws ServiceException {
        if (id == null) {
            throw new ServiceException(CARD_ID_MUST_NOT_BE_NULL);
        }
        if (dto == null) {
            throw new ServiceException(CARD_DTO_MUST_NOT_BE_NULL);
        }

        int updated = cardRepository.updateCardById(id, dto.getNumber(), dto.getHolder());
        if (updated == 0) {
            throw new ServiceException(CARD_NOT_UPDATED + id);
        }

        return getById(id);
    }
}
