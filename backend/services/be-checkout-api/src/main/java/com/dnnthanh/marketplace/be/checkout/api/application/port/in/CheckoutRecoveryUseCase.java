package com.dnnthanh.marketplace.be.checkout.api.application.port.in;

import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutResult;

public interface CheckoutRecoveryUseCase {
    CheckoutResult recover(String checkoutKey);
}
