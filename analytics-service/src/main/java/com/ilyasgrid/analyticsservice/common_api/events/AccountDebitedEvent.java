package com.ilyasgrid.analyticsservice.common_api.events;

public record AccountDebitedEvent (
        String id,
        double amount,
        String currency
) {
}
