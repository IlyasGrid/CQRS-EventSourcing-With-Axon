package com.ilyasgrid.analyticsservice.queries.controllers;

import com.ilyasgrid.analyticsservice.common_api.queries.GetBankStatsQuery;
import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/queries/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Query API", description = "Query endpoints for bank analytics and statistics")
public class AnalyticsQueryController {

    private final QueryGateway queryGateway;

    @GetMapping("/stats")
    @Operation(summary = "Get Bank Statistics", description = "Retrieve current bank statistics including total balance and account count using QueryGateway")
    public CompletableFuture<BankStats> getBankStats() {
        return queryGateway.query(
                new GetBankStatsQuery(),
                ResponseTypes.instanceOf(BankStats.class)
        );
    }
}
