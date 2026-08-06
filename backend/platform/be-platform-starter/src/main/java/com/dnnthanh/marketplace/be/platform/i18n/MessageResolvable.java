package com.dnnthanh.marketplace.be.platform.i18n;

/**
 * Project i18n contract that is also a Spring {@link
 * org.springframework.context.MessageSourceResolvable}. This lets platform error enums and Spring
 * validation errors share one resolver abstraction.
 */
public interface MessageResolvable extends org.springframework.context.MessageSourceResolvable {

    /** MessageSource lookup key. */
    String getMessageKey();

    @Override
    default String[] getCodes() {
        return new String[] {getMessageKey()};
    }

    @Override
    default Object[] getArguments() {
        return null;
    }

    /** Fallback when the configured bundle does not contain the message key. */
    @Override
    default String getDefaultMessage() {
        return getMessageKey();
    }
}
