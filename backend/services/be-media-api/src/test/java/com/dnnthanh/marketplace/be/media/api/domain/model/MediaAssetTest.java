package com.dnnthanh.marketplace.be.media.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.media.api.domain.exception.MediaStateConflictException;
import org.junit.jupiter.api.Test;

class MediaAssetTest {
    @Test
    void variantGenerationIsIdempotentAndReferencedAssetCannotBeDeleted() {
        MediaAsset m = new MediaAsset("M", "S", "hash");
        m.uploaded();
        m.scanning();
        m.scanPassed();
        assertTrue(m.variantGenerated("240.webp"));
        assertFalse(m.variantGenerated("240.webp"));
        m.ready();
        m.attach();
        assertThrows(MediaStateConflictException.class, m::requestDelete);
    }
}
