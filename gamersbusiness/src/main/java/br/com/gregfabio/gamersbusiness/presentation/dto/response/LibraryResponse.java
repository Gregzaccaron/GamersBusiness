package br.com.gregfabio.gamersbusiness.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.gregfabio.gamersbusiness.domain.model.LibraryEntry;

public record LibraryResponse(Long id, Long gameId, Instant acquiredAt, BigDecimal paidPrice, int hoursPlayed) {
    public static LibraryResponse from(LibraryEntry entry) {
        return new LibraryResponse(
                entry.id(), entry.gameId(), entry.acquiredAt(), entry.paidPrice(), entry.hoursPlayed());
    }
}
