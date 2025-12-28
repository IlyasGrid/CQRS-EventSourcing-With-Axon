package com.ilyasgrid.eventsoucingwithaxon.common.dtos;

public record CreditAccountDTO(
        String id,
        double amount,
        String currency
) {
}
