package com.dnnthanh.marketplace.be.inventory.api.api.request;

import java.util.List;

public record ReleaseReservationsRequest(List<String> reservationKeys) {}
