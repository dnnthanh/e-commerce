package com.dnnthanh.marketplace.be.catalog.api.domain.model;

import com.dnnthanh.marketplace.be.catalog.api.domain.enumtype.AttributeType;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.InvalidAttributeValueException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Dynamic attribute definition with category applicability and typed validation. */
public record AttributeDefinition(
        Long attributeId,
        String code,
        AttributeType type,
        boolean required,
        boolean variantGenerating,
        List<String> allowedValues,
        BigDecimal minNumber,
        BigDecimal maxNumber) {
    public AttributeDefinition {
        Objects.requireNonNull(attributeId);
        Objects.requireNonNull(code);
        Objects.requireNonNull(type);
        allowedValues = allowedValues == null ? List.of() : List.copyOf(allowedValues);
    }

    public void validate(Object value) {
        if (value == null) {
            if (required)
                throw new InvalidAttributeValueException(code, "Required attribute missing");
            return;
        }
        switch (type) {
            case TEXT -> {
                if (!(value instanceof String)) throw invalid();
            }
            case BOOLEAN -> {
                if (!(value instanceof Boolean)) throw invalid();
            }
            case NUMBER -> {
                if (!(value instanceof Number number)) throw invalid();
                BigDecimal n = new BigDecimal(number.toString());
                if (minNumber != null && n.compareTo(minNumber) < 0) throw invalid();
                if (maxNumber != null && n.compareTo(maxNumber) > 0) throw invalid();
            }
            case SINGLE_SELECT -> {
                if (!(value instanceof String string) || !allowedValues.contains(string))
                    throw invalid();
            }
            case MULTI_SELECT -> {
                if (!(value instanceof List<?> list) || !allowedValues.containsAll(list))
                    throw invalid();
            }
        }
    }

    private InvalidAttributeValueException invalid() {
        return new InvalidAttributeValueException(code);
    }
}
