package com.dnnthanh.marketplace.be.platform.mapping;

import org.mapstruct.MappingTarget;

/**
 * Opt-in partial update contract.
 *
 * <p>{@link PlatformMapperConfig} ignores null source properties for this mapping, so callers can
 * patch only values that are actually present.
 */
public interface PatchMapper<SOURCE, TARGET> extends MapperContract {

    void update(SOURCE source, @MappingTarget TARGET target);
}
