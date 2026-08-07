package com.dnnthanh.marketplace.be.platform.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

/** Regression coverage for the class-based proxy used by the request-scoped UserContext bean. */
class UserContextProxyabilityTest {

    @Test
    void userContextCanBeClassProxiedBySpring() {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(new UserContext("user-1", "alice", UserContext.ActorType.USER, Set.of("BUYER")));
        proxyFactory.setProxyTargetClass(true);

        Object proxy = assertDoesNotThrow(proxyFactory::getProxy);

        assertEquals(UserContext.class, proxyFactory.getTargetClass());
    }
}
