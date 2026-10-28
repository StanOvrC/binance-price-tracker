package com.shokal.binancepricetracker.service;

import com.shokal.binancepricetracker.BinanceClient;
import com.shokal.binancepricetracker.dto.AvgPriceResponse;
import com.shokal.binancepricetracker.dto.BinancePriceResponse;
import com.shokal.binancepricetracker.dto.PagedPriceResponse;
import com.shokal.binancepricetracker.dto.PriceResponse;
import com.shokal.binancepricetracker.entity.Price;
import com.shokal.binancepricetracker.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
        return mapToPriceResponse(latest);
    }

    public AvgPriceResponse getAvgPrice(OffsetDateTime from, OffsetDateTime to) {
        BigDecimal avgPrice = priceRepository.findAllByFetchedAtFromTo(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No prices found for the given period"));
        return new AvgPriceResponse(avgPrice);
    }

    public PagedPriceResponse getAllByFetchedAt(OffsetDateTime from, OffsetDateTime to, int page, int size) {
        Page<Price> pricePage = priceRepository.findAllByFetchedAtBetween(from, to, PageRequest.of(page, size));

        List<PriceResponse> items = pricePage.getContent().stream()
                .map(this::mapToPriceResponse)
                .toList();

        return new PagedPriceResponse(
                items,
                pricePage.getNumber(),
                pricePage.getSize(),
                pricePage.getTotalElements(),
                pricePage.hasNext()
        );
    }

    private Price mapToPrice(BinancePriceResponse response) {
        Price price = new Price();
        price.setSymbol("BNBUSDT");
        price.setPrice(response.price());
        return price;
    }

    private PriceResponse mapToPriceResponse(Price price) {
        return new PriceResponse(price.getSymbol(), price.getPrice(),
                price.getFetchedAt());
    }
}
