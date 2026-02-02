package com.innowise.task.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardDTO {

    private final static String NUMBER_PATTERN = "\\d{16}";
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    @Pattern(regexp = NUMBER_PATTERN)
    private String number;

    @NotBlank
    @Size(max = 255)
    private String holder;

    @NotNull
    @Future
    private LocalDate expirationDate;

    @NotNull
    private Boolean active;
}
