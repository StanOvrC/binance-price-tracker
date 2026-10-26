package com.shokal.binancepricetracker.service;

import com.shokal.binancepricetracker.BinanceClient;
import com.shokal.binancepricetracker.dto.AvgPriceResponse;
import com.shokal.binancepricetracker.dto.BinancePriceResponse;
import com.shokal.binancepricetracker.dto.PriceResponse;
import com.shokal.binancepricetracker.entity.Price;
import com.shokal.binancepricetracker.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final BinanceClient binanceClient;
    private final PriceRepository priceRepository;

    @Scheduled(fixedDelayString = "${BINANCE_POLL_INTERVAL:PT30S}")
    public void save() {
        BinancePriceResponse response = binanceClient.getCurrentPrice();
        priceRepository.save(mapToPrice(response));
    }

    public PriceResponse getCurrentPrice() {
        Price latest = priceRepository.findFirstByOrderByFetchedAtDesc()
                .orElseThrow(() -> new RuntimeException("No price records found"));
        return mapToPriceRequest(latest);
    }

    public AvgPriceResponse getAvgPrice(OffsetDateTime from, OffsetDateTime to) {
        BigDecimal avgPrice = priceRepository.findAllByFetchedAtFromTo(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No prices found for the given period"));
        return new AvgPriceResponse(avgPrice);
    }

    private Price mapToPrice(BinancePriceResponse response) {
        Price price = new Price();
        price.setSymbol("BNBUSDT");
        price.setPrice(response.price());
        return price;
    }

    private PriceResponse mapToPriceRequest(Price price) {
        return new PriceResponse(price.getSymbol(), price.getPrice(),
                price.getFetchedAt());
    }
}
