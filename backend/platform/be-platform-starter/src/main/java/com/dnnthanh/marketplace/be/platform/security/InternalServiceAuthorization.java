package com.dnnthanh.marketplace.be.platform.security;

import com.dnnthanh.marketplace.be.platform.context.UserContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

/** Authorizes internal service-account calls without treating them as human administrators. */
@Component("internalServiceAuthorization")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InternalServiceAuthorization {

    private final UserContext userContext;

    public InternalServiceAuthorization(UserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * Checks whether the caller uses an internal service account.
     *
     * @return {@code true} only for a Keycloak service account
     */
    public boolean isServiceAccount() {
        return userContext.roles().contains("SERVICE_ACCOUNT");
    }
}
