package com.dnnthanh.marketplace.be.returns.api.domain.constant;

import lombok.experimental.UtilityClass;

/** Technical/business limits shared by Return use cases. */
@UtilityClass
public class ReturnConstants {
    public static final int MAX_RECENT_RETURNS = 100;
    public static final int RETURN_WINDOW_DAYS = 30;
    public static final String RETURN_KEY_PREFIX = "RET-";
    public static final String REFUND_KEY_PREFIX = "RF-";
}
