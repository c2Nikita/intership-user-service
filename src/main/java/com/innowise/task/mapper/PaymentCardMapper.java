package com.innowise.task.mapper;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    PaymentCardMapper INSTANCE = Mappers.getMapper(PaymentCardMapper.class);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDTO toDTO(PaymentCard paymentCard);

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);
}
