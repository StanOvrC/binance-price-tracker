package com.shokal.binancepricetracker;

import com.shokal.binancepricetracker.dto.BinancePriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BinanceClient {
    private static final String AVG_PRICE_PATH = "/api/v3/avgPrice";
    private static final String SYMBOL_PARAM = "symbol";

    private final RestClient restClient;

    @Value("${binance.api.symbol}")
    private String symbol;

    public BinancePriceResponse getCurrentPrice() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(AVG_PRICE_PATH)
                        .queryParam(SYMBOL_PARAM, symbol)
                        .build())
                .retrieve()
                .body(BinancePriceResponse.class);
    }
}
