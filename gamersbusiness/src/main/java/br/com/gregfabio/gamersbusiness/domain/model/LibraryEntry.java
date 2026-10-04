package br.com.gregfabio.gamersbusiness.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LibraryEntry(
        Long id,
        Long userId,
        Long gameId,
        Instant acquiredAt,
        BigDecimal paidPrice,
        int hoursPlayed) {
}
