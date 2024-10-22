package com.example.rms.common.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(Include.NON_NULL)
public record ErrorResponse (
        @JsonProperty("error") String error,
        @JsonProperty("message") String message,
        @JsonProperty("details") List<String> details
){
    public ErrorResponse(String error, String message) {
        this(error, message, null);
    }
}
