package com.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactUploadDto {

    @NotBlank(message = "userId is required")
    private String userId; // in a real app this comes from the auth principal, not the body

    @NotEmpty(message = "contacts list must not be empty")
    @Size(max = 5000, message = "a single upload batch cannot exceed 5000 contacts")
    @Valid
    private List<ContactDto> contacts;
}
