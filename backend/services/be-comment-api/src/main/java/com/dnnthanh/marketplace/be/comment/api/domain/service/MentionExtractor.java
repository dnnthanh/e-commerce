package com.dnnthanh.marketplace.be.comment.api.domain.service;

import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts de-duplicated username mentions without coupling the domain to notification transport.
 */
public final class MentionExtractor {
    private static final Pattern MENTION = Pattern.compile(CommentConstants.MENTION_PATTERN);

    /** Returns mentioned usernames in first-seen order. */
    public Set<String> extract(String content) {
        Set<String> mentions = new LinkedHashSet<>();
        if (content == null) {
            return mentions;
        }
        Matcher matcher = MENTION.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return Set.copyOf(mentions);
    }
}
