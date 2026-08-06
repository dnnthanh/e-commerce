package com.dnnthanh.marketplace.be.platform.mapping;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct policy for every backend service.
 *
 * <p>Unknown target fields are intentionally ignored so transport/domain/entity models can evolve
 * independently. Null source properties are ignored for {@code @MappingTarget} update mappings,
 * which makes partial update/patch mappings safe by default.
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlatformMapperConfig {}
