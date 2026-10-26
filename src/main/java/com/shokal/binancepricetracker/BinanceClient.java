package com.shokal.binancepricetracker;

import com.shokal.binancepricetracker.dto.BinancePriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BinanceClient {
    private final RestClient restClient;

    public BinancePriceResponse getCurrentPrice() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v3/avgPrice")
                        .queryParam("symbol", "BNBUSDT")
                        .build())
                .retrieve()
                .body(BinancePriceResponse.class);
    }
}
