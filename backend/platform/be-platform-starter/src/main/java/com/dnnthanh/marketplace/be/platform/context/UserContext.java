package com.dnnthanh.marketplace.be.platform.context;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable execution identity exposed to application code instead of Spring Security primitives.
 *
 * <p>This type intentionally remains non-final because Spring creates a class-based scoped proxy for
 * the request-scoped {@code UserContext} bean. Record types are implicitly final and therefore cannot
 * be proxied with CGLIB.
 */
public class UserContext {
    private final String userId;
    private final String username;
    private final ActorType actorType;
    private final Set<String> roles;

    public UserContext(String userId, String username, ActorType actorType, Set<String> roles) {
        this.userId = userId;
        this.username = username;
        this.actorType = actorType;
        this.roles = roles;
    }

    public String userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public ActorType actorType() {
        return actorType;
    }

    public Set<String> roles() {
        return roles;
    }

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserContext that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(username, that.username)
                && actorType == that.actorType
                && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, actorType, roles);
    }

    @Override
    public String toString() {
        return "UserContext[userId="
                + userId
                + ", username="
                + username
                + ", actorType="
                + actorType
                + ", roles="
                + roles
                + "]";
    }
}
