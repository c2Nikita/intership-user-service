package com.innowise.task.service.impl;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import com.innowise.task.entity.User;
import com.innowise.task.exception.BusinessRuleException;
import com.innowise.task.exception.NotFoundException;
import com.innowise.task.exception.ServiceException;
import com.innowise.task.exception.ValidationException;
import com.innowise.task.mapper.PaymentCardMapper;
import com.innowise.task.repository.PaymentCardRepository;
import com.innowise.task.repository.UserRepository;
import com.innowise.task.service.PaymentCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    public static final String USER_NOT_FOUND = "User not found with id ";

    @Autowired
    private PaymentCardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;


    @CachePut(value = "cards", key = "#result.id")
    @CacheEvict(value = "cardsByUserId", key = "#result.userId")
    @Transactional
    @Override
    public PaymentCardDTO create(PaymentCardDTO dto) {
        if (dto == null) {
            throw new ValidationException(CARD_DTO_MUST_NOT_BE_NULL);
        }

        if (dto.getUserId() == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + dto.getUserId()));

        List<PaymentCard> existingCards = cardRepository.findAllByUserId(dto.getUserId());
        if (existingCards.size() >= 5) {
            throw new BusinessRuleException(USER_ID_MUST_NOT_HAVE_MORE_THAN_FIVE_CARDS);
        }

        PaymentCard card = PaymentCardMapper.INSTANCE.toEntity(dto);
        card.setUser(user);
        PaymentCard saved = cardRepository.save(card);

        return PaymentCardMapper.INSTANCE.toDTO(saved);
    }

    @Cacheable(value = "cards", key = "#id")
    @Override
    public PaymentCardDTO getById(Long id) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }

        return cardRepository.findById(id)
                .map(PaymentCardMapper.INSTANCE::toDTO)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND + id));
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
    public void setActiveStatus(Long id, boolean active) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }

        int updated = cardRepository.setActiveStatus(id, active);

        if (updated == 0) {
            throw new NotFoundException(CARD_NOT_UPDATED + id);
        }

        PaymentCard paymentCard = cardRepository.getById(id);
        cacheManager.getCache("cards").evict(id);
        cacheManager.getCache("cardsByUserId").evict(paymentCard.getUser().getId());
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }

        PaymentCard paymentCard = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND + id));

        cardRepository.deleteById(id);
        cacheManager.getCache("cards").evict(id);
        cacheManager.getCache("cardsByUserId").evict(paymentCard.getUser().getId());
    }

    @Cacheable(value = "cardsByUserId", key = "#userId")
    @Override
    public List<PaymentCardDTO> getAllByUserId(Long userId) {
        if (userId == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        return cardRepository.findAllByUserId(userId)
                .stream()
                .map(PaymentCardMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "cardsByUserId", key = "#dto.userId")
    })
    @Transactional
    @Override
    public PaymentCardDTO update(Long id, PaymentCardDTO dto) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }
        if (dto == null) {
            throw new ValidationException(CARD_DTO_MUST_NOT_BE_NULL);
        }

        int updated = cardRepository.updateCardById(id, dto.getNumber(), dto.getHolder());
        if (updated == 0) {
            throw new NotFoundException(CARD_NOT_UPDATED + id);
        }

        return getById(id);
    }
}
