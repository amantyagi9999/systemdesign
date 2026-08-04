package com.demo.dto;

import com.demo.dto.enums.SpamCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpamDto {

    private String phoneNumber;
    private String reason;

    @NotBlank(message = "category is required")
    private SpamCategory category;

    @NotBlank(message = "reportedBy is required")
    private String reportedBy;
}
