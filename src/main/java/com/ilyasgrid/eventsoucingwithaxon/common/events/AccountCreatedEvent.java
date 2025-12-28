package com.ilyasgrid.eventsoucingwithaxon.common.events;

import com.ilyasgrid.eventsoucingwithaxon.common.enums.AccountStatus;

public record AccountCreatedEvent
        (String accountId, double initialBalance,
         String currency,
         AccountStatus accountStatus) {
}
