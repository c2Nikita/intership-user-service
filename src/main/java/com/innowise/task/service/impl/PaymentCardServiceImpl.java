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
import com.innowise.task.specification.PaymentCardSpecification;
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

    private static final String CARD_NOT_FOUND = "Payment card not found with id ";
    private static final String CARD_ID_MUST_NOT_BE_NULL = "Payment card id must not be null";
    private static final String CARD_DTO_MUST_NOT_BE_NULL = "Payment card DTO must not be null";
    private static final String USER_ID_MUST_NOT_BE_NULL = "User id must not be null";
    private static final String USER_ID_MUST_NOT_HAVE_MORE_THAN_FIVE_CARDS = "User cannot have more than 5 cards";
    private static final String USER_NOT_FOUND = "User not found with id ";

    private PaymentCardRepository cardRepository;

    private UserRepository userRepository;

    private PaymentCardMapper paymentCardMapper;

    public PaymentCardServiceImpl(PaymentCardRepository cardRepository,
                                  UserRepository userRepository,
                                  PaymentCardMapper paymentCardMapper) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }


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

        PaymentCard card = paymentCardMapper.toEntity(dto);
        card.setUser(user);
        PaymentCard saved = cardRepository.save(card);

        return paymentCardMapper.toDTO(saved);
    }

    @Cacheable(value = "cards", key = "#id")
    @Override
    public PaymentCardDTO getById(Long id) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }

        return cardRepository.findById(id)
                .map(paymentCardMapper::toDTO)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND + id));
    }

    @Override
    public Page<PaymentCardDTO> findAll(String name, String surname, Pageable pageable)
            throws ServiceException {

        Specification<PaymentCard> specification = Specification.where(PaymentCardSpecification.userHasName(name))
                .and(PaymentCardSpecification.userHasSurname(surname));

        return cardRepository.findAll(specification, pageable)
                .map(paymentCardMapper::toDTO);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "cardsByUserId", key = "#result.userId")
    })
    @Override
    public PaymentCardDTO setActiveStatus(Long id, boolean active) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }

        PaymentCard card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND + id));

        card.setActive(active);

        return paymentCardMapper.toDTO(card);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "cardsByUserId", key = "#result.userId")
    })
    @Override
    public PaymentCardDTO delete(Long id) {
        if (id == null) {
            throw new ValidationException(CARD_ID_MUST_NOT_BE_NULL);
        }

        PaymentCard paymentCard = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND + id));

        cardRepository.delete(paymentCard);

        return paymentCardMapper.toDTO(paymentCard);
    }

    @Cacheable(value = "cardsByUserId", key = "#userId")
    @Override
    public List<PaymentCardDTO> getAllByUserId(Long userId) {
        if (userId == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }

        return cardRepository.findAllByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDTO)
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

        PaymentCard card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND + id));

        card.setNumber(dto.getNumber());
        card.setHolder(dto.getHolder());

        return paymentCardMapper.toDTO(card);
    }

    @Override
    public Long getOwnerId(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"))
                .getUser()
                .getId();
    }
}
