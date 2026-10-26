package com.shokal.binancepricetracker.dto;

import java.math.BigDecimal;

public record AvgPriceResponse(
        BigDecimal averagePrice
) {
}
