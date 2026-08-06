package com.dnnthanh.marketplace.be.platform.i18n;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/**
 * Optional extension for enum values that need a localized display label in addition to a stable
 * JSON code. Internal/event enums should normally implement {@link CodeEnum} only.
 */
public interface I18nCodeEnum extends CodeEnum, MessageResolvable {

    @Override
    default String getMessageKey() {
        if (this instanceof Enum<?> enumValue) {
            return "enum." + enumValue.getDeclaringClass().getSimpleName() + "." + getCode();
        }
        throw new IllegalStateException("I18nCodeEnum must be implemented by an enum type");
    }

    @Override
    default String getDefaultMessage() {
        return getCode();
    }
}
