package com.innowise.task.repository;

import com.innowise.task.entity.PaymentCard;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    PaymentCard save(PaymentCard paymentCard);

    Optional<PaymentCard> findById(Long id);

    Page<PaymentCard> findAll(Specification<PaymentCard> specification, Pageable pageable);

    List<PaymentCard> findAllByUserId(Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PaymentCard c SET c.number = :number, c.holder = :holder WHERE c.id = :id")
    int updateCardById(@Param("id") Long id,
                       @Param("number") String number,
                       @Param("holder") String holder);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :id", nativeQuery = true)
    int setActiveStatus(@Param("id") Long id, @Param("active") boolean active);
}
