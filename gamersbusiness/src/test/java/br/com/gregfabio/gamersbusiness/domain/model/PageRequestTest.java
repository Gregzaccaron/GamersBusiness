package br.com.gregfabio.gamersbusiness.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.gregfabio.gamersbusiness.domain.error.DomainException;

class PageRequestTest {
    @Test
    void acceptsTheInclusivePageSizeBoundaries() {
        PageRequest minimum = new PageRequest(0, 1);
        PageRequest maximum = new PageRequest(3, 100);
        assertEquals(0, minimum.page());
        assertEquals(1, minimum.size());
        assertEquals(3, maximum.page());
        assertEquals(100, maximum.size());
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
