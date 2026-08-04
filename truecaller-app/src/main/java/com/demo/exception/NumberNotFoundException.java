package com.demo.exception;

import org.springframework.http.HttpStatus;

public class NumberNotFoundException extends ApiException {

    public NumberNotFoundException(String number) {
        super("NUMBER_NOT_FOUND",
              "No caller-id information found for number: " + number,
              HttpStatus.NOT_FOUND);
    }
}
