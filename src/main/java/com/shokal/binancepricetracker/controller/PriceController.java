package com.shokal.binancepricetracker.controller;

import com.shokal.binancepricetracker.dto.AvgPriceResponse;
import com.shokal.binancepricetracker.dto.PagedPriceResponse;
import com.shokal.binancepricetracker.dto.PriceResponse;
import com.shokal.binancepricetracker.dto.TrendResponse;
import com.shokal.binancepricetracker.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {
    private final PriceService priceService;

    @GetMapping("/latest")
    public ResponseEntity<PriceResponse> getCurrentPrice() {
        return ResponseEntity.ok(priceService.getCurrentPrice());
    }

    @GetMapping("/average")
    public ResponseEntity<AvgPriceResponse> getAvgPrice(
            @RequestParam("from") OffsetDateTime from,
            @RequestParam("to") OffsetDateTime to) {
        return ResponseEntity.ok(priceService.getAvgPrice(from, to));
    }

    @GetMapping()
    public ResponseEntity<PagedPriceResponse> getAllPricesByFetchedTime(
            @RequestParam("from") OffsetDateTime from,
            @RequestParam("to") OffsetDateTime to,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return ResponseEntity.ok(priceService.getAllByFetchedAt(from, to, page, size));
    }

    @GetMapping("/trend")
    public ResponseEntity<TrendResponse> getTrendByTime(
            @RequestParam("from") OffsetDateTime from,
            @RequestParam("to") OffsetDateTime to) {
        return ResponseEntity.ok(priceService.getTrendByTime(from, to));
    }
}
