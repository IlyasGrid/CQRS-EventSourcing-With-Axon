package com.ilyasgrid.analyticsservice.common_api.events;

import com.ilyasgrid.analyticsservice.common_api.enums.AccountStatus;

public record AccountCreatedEvent
        (String accountId, double initialBalance,
         String currency,
         AccountStatus accountStatus) {
}
