package com.shokal.binancepricetracker.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PriceResponse(
        String symbol,
        BigDecimal price,
        OffsetDateTime fetchedAt) {
}
