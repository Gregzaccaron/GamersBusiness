package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(Instant timestamp, int status, String message, String path, Map<String, String> errors) {
    public ApiErrorResponse {
        errors = errors == null ? Map.of() : Map.copyOf(errors);
    }
}
