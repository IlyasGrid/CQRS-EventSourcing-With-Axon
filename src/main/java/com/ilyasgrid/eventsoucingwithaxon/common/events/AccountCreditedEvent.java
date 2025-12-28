package com.ilyasgrid.eventsoucingwithaxon.common.events;

public record AccountCreditedEvent(
        String id,
        double amount,
        String currency
) {
}
