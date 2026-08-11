package com.dnnthanh.marketplace.be.comment.api.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;

class CommentApiContractTest {

    @Test
    void exposesBrowserSafeModerationCommandsWithModeratorPermission() {
        assertThat(postMappings(CommentApi.class))
                .contains(
                        "/private/comments/{threadId}/hide",
                        "/private/comments/{threadId}/unhide");

        assertThat(preAuthorizeExpressions(CommentApi.class, "hide", "unhide"))
                .hasSize(2)
                .allMatch(expression -> expression.contains("COMMENT_MODERATE"));
    }

    private static List<String> postMappings(Class<?> apiType) {
        return Arrays.stream(apiType.getMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .toList();
    }

    private static List<String> preAuthorizeExpressions(Class<?> apiType, String... methodNames) {
        var names = List.of(methodNames);
        return Arrays.stream(apiType.getMethods())
                .filter(method -> names.contains(method.getName()))
                .map(CommentApiContractTest::preAuthorize)
                .filter(expression -> expression != null)
                .toList();
    }

    private static String preAuthorize(Method method) {
        var annotation = method.getAnnotation(PreAuthorize.class);
        return annotation == null ? null : annotation.value();
    }
}
