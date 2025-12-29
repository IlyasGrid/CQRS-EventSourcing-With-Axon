package com.ilyasgrid.analyticsservice.common_api.events;

public record AccountCreditedEvent(
        String id,
        double amount,
        String currency
) {
}
