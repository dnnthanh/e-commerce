package com.dnnthanh.marketplace.be.platform.context;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.util.Set;

/**
 * Immutable execution identity exposed to application code instead of Spring Security primitives.
 *
 * @param userId authenticated user or service identifier
 * @param username display/login name
 * @param actorType execution actor type
 * @param roles effective high-level roles from the token
 */
public record UserContext(String userId, String username, ActorType actorType, Set<String> roles) {

    /** Execution actor types used by audit and authorization. */
    public enum ActorType implements CodeEnum {
        USER,
        SERVICE,
        SCHEDULER,
        KAFKA_CONSUMER
    }

    public static UserContext system(String serviceName, ActorType actorType) {
        return new UserContext(serviceName, serviceName, actorType, Set.of("SERVICE_ACCOUNT"));
    }
}
