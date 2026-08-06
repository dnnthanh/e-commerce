package com.dnnthanh.marketplace.be.comment.api.config;

import com.dnnthanh.marketplace.be.comment.api.domain.service.MentionExtractor;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Explicit comment domain/application dependencies. */
@Configuration
public class CommentConfiguration {

    /** UTC clock used by deterministic application tests. */
    @Bean
    Clock commentClock() {
        return Clock.systemUTC();
    }

    /** Dedicated mention parser used by comment write flows. */
    @Bean
    MentionExtractor mentionExtractor() {
        return new MentionExtractor();
    }
}
