package com.shokal.binancepricetracker.controller;

import com.shokal.binancepricetracker.dto.AvgPriceResponse;
import com.shokal.binancepricetracker.dto.PagedPriceResponse;
import com.shokal.binancepricetracker.dto.PriceResponse;
import com.shokal.binancepricetracker.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        return new ResponseEntity<>(priceService.getCurrentPrice(), HttpStatus.OK);
    }

    @GetMapping("/average")
    public ResponseEntity<AvgPriceResponse> getAvgPrice(
            @RequestParam("from") OffsetDateTime from,
            @RequestParam("to") OffsetDateTime to) {
        return new ResponseEntity<>(priceService.getAvgPrice(from, to), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<PagedPriceResponse> getAllPricesByFetchedTime(
            @RequestParam("from") OffsetDateTime from,
            @RequestParam("to") OffsetDateTime to,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return new ResponseEntity<>(priceService.getAllByFetchedAt(from, to, page, size), HttpStatus.OK);
    }
}
