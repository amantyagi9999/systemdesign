package com.tutorial.sd.parkinglot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DefaultApiResponse {

    private String message;
    private boolean success;
    private Object data;
    private int status;
}
