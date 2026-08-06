package com.dnnthanh.marketplace.be.platform.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Global Jackson 3 string deserializer that trims normal API input while respecting {@link NoTrim}.
 */
public final class TrimmedStringDeserializer extends ValueDeserializer<String> {

    private final boolean noTrim;

    public TrimmedStringDeserializer() {
        this(false);
    }

    private TrimmedStringDeserializer(boolean noTrim) {
        this.noTrim = noTrim;
    }

    /** {@inheritDoc} */
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {
        String value = parser.getValueAsString();
        return value == null || noTrim ? value : value.trim();
    }

    /** {@inheritDoc} */
    @Override
    public ValueDeserializer<?> createContextual(
            DeserializationContext context, BeanProperty property) {
        if (property == null) {
            return this;
        }
        return new TrimmedStringDeserializer(property.getAnnotation(NoTrim.class) != null);
    }
}
