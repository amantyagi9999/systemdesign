package com.demo.event;

import com.demo.dto.enums.SpamCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpamReportEvent {

    private String phoneNumber;
    private SpamCategory category;
    private String reportedBy;
    private LocalDateTime reportedAt;

}