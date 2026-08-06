package com.dnnthanh.marketplace.be.platform.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Closed-set contract exposing a stable JSON/API code. Enums default to their current constant name
 * so adopting the contract does not silently break existing wire values; an enum may override this
 * method when its external code intentionally differs from the Java identifier.
 */
public interface CodeEnum {

    /** Stable external code serialized at JSON boundaries. */
    @JsonValue
    default String getCode() {
        if (this instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        throw new IllegalStateException("CodeEnum must be implemented by an enum type");
    }
}
