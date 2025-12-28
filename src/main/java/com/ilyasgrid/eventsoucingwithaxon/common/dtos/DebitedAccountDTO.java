package com.ilyasgrid.eventsoucingwithaxon.common.dtos;

public record DebitedAccountDTO(
        String id,
        double amount,
        String currency
) {
}
