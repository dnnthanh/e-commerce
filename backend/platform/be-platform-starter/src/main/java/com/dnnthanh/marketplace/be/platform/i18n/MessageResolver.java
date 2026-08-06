package com.dnnthanh.marketplace.be.platform.i18n;

import java.util.Locale;
import org.springframework.context.MessageSourceResolvable;

/**
 * Resolves project or Spring message-resolvable values using default or explicit locale semantics.
 */
public interface MessageResolver {

    /** Resolves using the current request locale, falling back to the JVM default locale. */
    String resolve(MessageSourceResolvable message);

    /**
     * Resolves using an explicit locale. A {@code null} locale falls back to the default policy.
     */
    String resolve(MessageSourceResolvable message, Locale locale);

    /** Resolves with caller-provided interpolation arguments using the default locale policy. */
    String resolve(MessageSourceResolvable message, Object... arguments);

    /** Resolves with explicit locale and caller-provided interpolation arguments. */
    String resolve(MessageSourceResolvable message, Locale locale, Object... arguments);
}
