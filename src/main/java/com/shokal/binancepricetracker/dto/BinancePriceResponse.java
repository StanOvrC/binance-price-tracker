package com.shokal.binancepricetracker.dto;

import java.math.BigDecimal;

public record BinancePriceResponse(
        Integer mins,
        BigDecimal price,
        Long closeTime
) {
}
