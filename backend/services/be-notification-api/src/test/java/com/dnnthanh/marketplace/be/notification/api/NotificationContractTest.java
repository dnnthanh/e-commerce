package com.dnnthanh.marketplace.be.notification.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.notification.api.api.response.PreferenceView;
import org.junit.jupiter.api.Test;

class NotificationContractTest {

    @Test
    void preferenceCarriesRealtimeAndSellerControlsSeparately() {
        PreferenceView preference = new PreferenceView(true, false, false, false);

        assertTrue(preference.realtime());
        assertFalse(preference.sellerUpdates());
    }
}
