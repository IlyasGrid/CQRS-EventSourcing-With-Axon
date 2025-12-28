package com.ilyasgrid.eventsoucingwithaxon.common.events;

public record DebitedAccountEvent (
        String id,
        double amount,
        String currency
) {
}
