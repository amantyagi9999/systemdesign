package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactDto {

    @NotBlank(message = "number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "number must be in E.164 format, e.g. +919876543210")
    private String phoneNumber;

    @NotBlank(message = "name is required")
    private String name;

    private String photo;
}
