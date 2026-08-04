package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchDto {

    private String displayName;
    private String category;       // NONE / SPAM / SCAM / TELEMARKETER
    private String photoUrl;
    private boolean businessVerified;
}
