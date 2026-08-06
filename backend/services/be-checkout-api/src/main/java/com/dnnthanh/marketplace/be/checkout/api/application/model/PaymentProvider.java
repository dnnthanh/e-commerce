package com.dnnthanh.marketplace.be.checkout.api.application.model;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Payment provider selected by Checkout independently of the HTTP enum. */
public enum PaymentProvider implements CodeEnum {
    MOMO,
    VNPAY,
    VIETQR
}
