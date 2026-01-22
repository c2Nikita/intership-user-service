package com.innowise.task.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {

    private final static int MAX_SIZE_FOR_FIELD_DB = 255;
    private Long id;

    @NotBlank
    @Size(max = MAX_SIZE_FOR_FIELD_DB)
    private String name;

    @NotBlank
    @Size(max = MAX_SIZE_FOR_FIELD_DB)
    private String surname;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Email
    @Size(max = MAX_SIZE_FOR_FIELD_DB)
    private String email;

    @NotNull
    private Boolean active;
}
