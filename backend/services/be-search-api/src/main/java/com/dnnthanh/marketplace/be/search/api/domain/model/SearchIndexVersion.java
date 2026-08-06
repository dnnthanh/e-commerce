package com.dnnthanh.marketplace.be.search.api.domain.model;

import java.util.HashMap;
import java.util.Map;

/** Protects search projection from stale/out-of-order source events. */
public final class SearchIndexVersion {
    private final Map<String, Long> versions = new HashMap<>();

    public synchronized boolean accept(String documentId, long sourceVersion) {
        Long current = versions.get(documentId);
        if (current != null && sourceVersion <= current) return false;
        versions.put(documentId, sourceVersion);
        return true;
    }

    public long versionOf(String documentId) {
        return versions.getOrDefault(documentId, -1L);
    }
}
