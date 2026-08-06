package com.dnnthanh.marketplace.be.platform.mapping;

/**
 * Full CRUD mapping contract for components that genuinely own all four mapping boundaries.
 *
 * <p>Hexagonal adapters that only own one boundary should extend the smaller specialized contract
 * instead of using placeholder {@code Void} types.
 */
public interface BaseMapper<REQUEST, MODEL, ENTITY, RESPONSE>
        extends RequestModelMapper<REQUEST, MODEL>,
                ModelEntityMapper<MODEL, ENTITY>,
                ModelResponseMapper<MODEL, RESPONSE> {}
