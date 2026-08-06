package com.dnnthanh.marketplace.be.platform.i18n;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/** MessageSource-backed implementation shared by HTTP and non-HTTP callers. */
@Component
@RequiredArgsConstructor
public class SpringMessageResolver implements MessageResolver {

    private final MessageSource messageSource;

    @Override
    public String resolve(MessageSourceResolvable message) {
        return resolve(message, defaultLocale());
    }

    @Override
    public String resolve(MessageSourceResolvable message, Locale locale) {
        Locale effectiveLocale = locale == null ? defaultLocale() : locale;
        return messageSource.getMessage(message, effectiveLocale);
    }

    @Override
    public String resolve(MessageSourceResolvable message, Object... arguments) {
        return resolve(message, defaultLocale(), arguments);
    }

    @Override
    public String resolve(MessageSourceResolvable message, Locale locale, Object... arguments) {
        Locale effectiveLocale = locale == null ? defaultLocale() : locale;
        Object[] effectiveArguments = arguments == null ? new Object[0] : arguments;
        String[] codes = message.getCodes();
        String messageCode = codes == null || codes.length == 0 ? null : codes[0];
        if (messageCode == null) {
            return message.getDefaultMessage();
        }
        return messageSource.getMessage(
                messageCode, effectiveArguments, message.getDefaultMessage(), effectiveLocale);
    }

    private Locale defaultLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return locale == null ? Locale.getDefault() : locale;
    }
}
