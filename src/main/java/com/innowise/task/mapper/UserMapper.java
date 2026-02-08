package com.innowise.task.mapper;

import com.innowise.task.entity.User;
import com.innowise.task.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    User toEntity(UserDTO userDto);

}
