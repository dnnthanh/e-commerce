package com.dnnthanh.marketplace.be.checkout.api.application.port.in;

import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutResult;

public interface CheckoutUseCase {
    CheckoutResult checkout(CheckoutCommand command);
}
