package com.dnnthanh.marketplace.be.search.api.application.query;

import com.dnnthanh.marketplace.be.search.api.application.exception.InvalidSearchCriteriaException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import lombok.experimental.UtilityClass;

/** Opaque URL-safe transport encoding for OpenSearch search-after sort values. */
@UtilityClass
public class SearchCursor {
    private static final String SEPARATOR = "\u001F";

    public static String encode(List<String> sortValues) {
        String payload = String.join(SEPARATOR, sortValues);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static List<String> decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return List.of();
        }
        try {
            String decoded =
                    new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Arrays.asList(decoded.split(SEPARATOR, -1));
        } catch (IllegalArgumentException invalid) {
            throw new InvalidSearchCriteriaException("Search cursor is invalid");
        }
    }
}
