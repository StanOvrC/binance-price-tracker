package com.shokal.binancepricetracker.dto;

import java.util.List;

public record PagedPriceResponse(
        List<PriceResponse> items,
        Integer page,
        Integer size,
        Long totalElements,
        Boolean hasNext
) {
}
