package br.com.gregfabio.gamersbusiness.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.gregfabio.gamersbusiness.domain.error.DomainException;

class PageRequestTest {
    @Test
    void acceptsTheInclusivePageSizeBoundaries() {
        assertEquals(new PageRequest(0, 1), new PageRequest(0, 1));
        assertEquals(new PageRequest(3, 100), new PageRequest(3, 100));
    }

    @Test
    void rejectsNegativePagesAndSizesOutsideTheContract() {
        assertThrows(DomainException.class, () -> new PageRequest(-1, 20));
        assertThrows(DomainException.class, () -> new PageRequest(0, 0));
        assertThrows(DomainException.class, () -> new PageRequest(0, 101));
    }

    @Test
    void calculatesPartialAndEmptyPageCounts() {
        assertEquals(3, PageResult.of(List.of(1), new PageRequest(0, 10), 21).totalPages());
        assertEquals(0, PageResult.of(List.of(), new PageRequest(0, 10), 0).totalPages());
    }
}
