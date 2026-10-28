package com.studypilot.common;

/** The request is well formed but violates a business rule (HTTP 422). */
public class UnprocessableException extends RuntimeException {

    public UnprocessableException(String message) {
        super(message);
    }
}
