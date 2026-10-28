package com.shokal.binancepricetracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shokal.binancepricetracker.entity.Trend;

import java.time.OffsetDateTime;

public record TrendResponse(
        String symbol,
        @JsonProperty("start_date")
        OffsetDateTime startDate,
        @JsonProperty("end_date")
        OffsetDateTime endDate,
        Trend trend
) {
}
