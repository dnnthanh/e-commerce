package com.dnnthanh.marketplace.be.search.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SearchIndexVersionTest {
    @Test
    void refusesDuplicateAndOlderSourceEvents() {
        SearchIndexVersion guard = new SearchIndexVersion();
        assertTrue(guard.accept("P", 3));
        assertFalse(guard.accept("P", 3));
        assertFalse(guard.accept("P", 2));
    }
}
