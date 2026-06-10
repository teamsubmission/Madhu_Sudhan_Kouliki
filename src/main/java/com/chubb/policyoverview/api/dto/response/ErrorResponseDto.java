package com.chubb.policyoverview.api.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponseDto {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
