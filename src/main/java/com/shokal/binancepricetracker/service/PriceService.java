package com.shokal.binancepricetracker.service;

import com.shokal.binancepricetracker.BinanceClient;
import com.shokal.binancepricetracker.dto.*;
import com.shokal.binancepricetracker.entity.Price;
import com.shokal.binancepricetracker.entity.Trend;
import com.shokal.binancepricetracker.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class PriceService {
    private final BinanceClient binanceClient;
    private final PriceRepository priceRepository;
    @Value("${binance.api.symbol}")
    private String defaultSymbol;

    @Scheduled(fixedDelayString = "${binance.poll.interval}")
    public void save() {
        try {
            BinancePriceResponse response = binanceClient.getCurrentPrice();
            if (response != null && response.price() != null) {
                Price saved = priceRepository.save(mapToPrice(response));
                log.info("Saved new price for {}: {}", saved.getSymbol(), saved.getPrice());
            }
        } catch (Exception e) {
            log.error("Failed to fetch price from Binance: {}", e.getMessage());
        }
    }

    public PriceResponse getCurrentPrice() {
        Price latest = priceRepository.findFirstByOrderByFetchedAtDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No prices found"));
        return mapToPriceResponse(latest);
    }

    public AvgPriceResponse getAvgPrice(OffsetDateTime from, OffsetDateTime to) {
        validateDates(from, to);
        BigDecimal avgPrice = priceRepository.findAllByFetchedAtFromTo(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No prices found for the given period"));
        return new AvgPriceResponse(avgPrice);
    }

    public PagedPriceResponse getAllByFetchedAt(OffsetDateTime from, OffsetDateTime to, int page, int size) {
        validateDates(from, to);
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

    public TrendResponse getTrendByTime(OffsetDateTime from, OffsetDateTime to) {
        validateDates(from, to);
        BigDecimal fromPrice = priceRepository.findFirstByFetchedAtBetweenOrderByFetchedAtAsc(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No prices found")).getPrice();
        BigDecimal toPrice = priceRepository.findFirstByFetchedAtBetweenOrderByFetchedAtDesc(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No prices found")).getPrice();

        Trend trend;
        if (fromPrice.compareTo(toPrice) > 0) {
            trend = Trend.DOWNWARD;
        } else if (fromPrice.compareTo(toPrice) < 0) {
            trend = Trend.UPWARD;
        } else {
            trend = Trend.FLAT;
        }

        return new TrendResponse(defaultSymbol, from, to, trend);
    }

    private void validateDates(OffsetDateTime from, OffsetDateTime to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date must be before 'to' date");
        }
    }

    private Price mapToPrice(BinancePriceResponse response) {
        Price price = new Price();
        price.setSymbol(defaultSymbol);
        price.setPrice(response.price());
        return price;
    }

    private PriceResponse mapToPriceResponse(Price price) {
        return new PriceResponse(price.getSymbol(), price.getPrice(),
                price.getFetchedAt());
    }
}
