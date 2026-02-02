package com.innowise.task.mapper;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    PaymentCardMapper INSTANCE = Mappers.getMapper(PaymentCardMapper.class);

    PaymentCardDTO toDTO(PaymentCard paymentCard);

    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);
}
