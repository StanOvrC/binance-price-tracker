package com.shokal.binancepricetracker.service;

import com.shokal.binancepricetracker.BinanceClient;
import com.shokal.binancepricetracker.dto.AvgPriceResponse;
import com.shokal.binancepricetracker.dto.BinancePriceResponse;
import com.shokal.binancepricetracker.dto.PagedPriceResponse;
import com.shokal.binancepricetracker.dto.PriceResponse;
import com.shokal.binancepricetracker.entity.Price;
import com.shokal.binancepricetracker.repository.PriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class PriceServiceTests {
    @Mock
    private BinanceClient binanceClient;
    @Mock
    private PriceRepository priceRepository;
    @InjectMocks
    private PriceService priceService;

    @Test
    @DisplayName("Test the price is saving if no exception occurs")
    public void shouldSavePriceWhenDataIsValid() {
        BinancePriceResponse response = new BinancePriceResponse(
                5, BigDecimal.valueOf(701.002), 1793363212330L
        );

        Price saved = new Price();
        saved.setSymbol("BNBUSDT");
        saved.setPrice(BigDecimal.valueOf(701.002));

        given(binanceClient.getCurrentPrice())
                .willReturn(response);
        given(priceRepository.save(any(Price.class)))
                .willReturn(saved);

        priceService.save();

        verify(binanceClient).getCurrentPrice();
        verify(priceRepository).save(any(Price.class));
    }

    @Test
    @DisplayName("Test the price is not saving " +
            "and exception is catching when occurs")
    public void shouldNotThrowExceptionWhenFailedToFetchBinancePrice() {
        given(binanceClient.getCurrentPrice())
                .willThrow(RuntimeException.class);

        assertDoesNotThrow(() -> priceService.save());

        verify(binanceClient).getCurrentPrice();
        verifyNoInteractions(priceRepository);
    }

    @Test
    @DisplayName("Test the price is not saving " +
            "when Binance response is null")
    public void shouldNotSavePriceWhenResponseIsNull() {
        given(binanceClient.getCurrentPrice())
                .willReturn(null);

        priceService.save();

        verify(binanceClient).getCurrentPrice();
        verifyNoInteractions(priceRepository);
    }

    @Test
    @DisplayName("Test the price is not saving " +
            "when Binance response price is null")
    public void shouldNotSavePriceWhenResponsePriceIsNull() {
        BinancePriceResponse response = new BinancePriceResponse(
                5, null, 1793363212330L
        );

        given(binanceClient.getCurrentPrice())
                .willReturn(response);

        priceService.save();

        verify(binanceClient).getCurrentPrice();
        verifyNoInteractions(priceRepository);
    }

    @Test
    @DisplayName("Test the current price returns if found")
    public void shouldReturnPriceResponseWhenPriceIsFound() {
        Price latest = new Price();
        latest.setId(999L);
        latest.setSymbol("BNBUSDT");
        latest.setPrice(BigDecimal.valueOf(701.002));


        given(priceRepository.findFirstByOrderByFetchedAtDesc())
                .willReturn(Optional.of(latest));

        PriceResponse result = priceService.getCurrentPrice();

        assertEquals(latest.getSymbol(), result.symbol());
        assertEquals(latest.getPrice(), result.price());
        assertEquals(latest.getFetchedAt(), result.fetchedAt());

        verify(priceRepository).findFirstByOrderByFetchedAtDesc();
    }

    @Test
    @DisplayName("Test the ResponseStatusException throws " +
            "when price is not found")
    public void shouldThrowResponseStatusExceptionWhenPriceIsNotFound() {
        given(priceRepository.findFirstByOrderByFetchedAtDesc())
                .willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> priceService.getCurrentPrice());

        verify(priceRepository).findFirstByOrderByFetchedAtDesc();
    }

    @Test
    @DisplayName("Test the average price returns " +
            "if price is found and no exception occurs")
    public void shouldReturnAvgPriceResponseWhenPricesIsFoundAndNoExceptionOccurs() {
        OffsetDateTime from = OffsetDateTime.of(
                2026, 10, 15, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        OffsetDateTime to = OffsetDateTime.of(
                2026, 10, 30, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        given(priceRepository.findAllByFetchedAtFromTo(from, to))
                .willReturn(Optional.of(BigDecimal.valueOf(701.002)));

        AvgPriceResponse avgPriceResponse = priceService.getAvgPrice(from, to);

        assertEquals(BigDecimal.valueOf(701.002), avgPriceResponse.averagePrice());

        verify(priceRepository).findAllByFetchedAtFromTo(from, to);
    }

    @Test
    @DisplayName("Test the ResponseStatusException throws " +
            "when dates are not valid")
    public void shouldThrowResponseStatusExceptionWhenDatesAreNotValid() {
        OffsetDateTime to = OffsetDateTime.of(
                2026, 10, 15, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        OffsetDateTime from = OffsetDateTime.of(
                2026, 10, 30, 10,
                15, 10, 15, ZoneOffset.UTC
        );

        assertThrows(ResponseStatusException.class,
                () -> priceService.getAvgPrice(from, to));

        verifyNoInteractions(priceRepository);
    }

    @Test
    @DisplayName("Test the ResponseStatusException throws " +
            "when no prices found for the given period")
    public void shouldThrowResponseStatusExceptionWhenNoPricesFound() {
        OffsetDateTime from = OffsetDateTime.of(
                2026, 10, 15, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        OffsetDateTime to = OffsetDateTime.of(
                2026, 10, 30, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        given(priceRepository.findAllByFetchedAtFromTo(from, to))
                .willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> priceService.getAvgPrice(from, to));

        verify(priceRepository).findAllByFetchedAtFromTo(from, to);
    }

    @Test
    @DisplayName("Test paged prices return when no exception occurs")
    public void shouldReturnPagedPriceResponseWhenNoExceptionOccurs() {
        OffsetDateTime from = OffsetDateTime.of(
                2026, 10, 15, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        OffsetDateTime to = OffsetDateTime.of(
                2026, 10, 30, 10,
                15, 10, 15, ZoneOffset.UTC
        );
        int pageNumber = 1;
        int size = 20;

        Price price = new Price();
        price.setId(99L);
        price.setSymbol("BNBUSDT");
        price.setPrice(BigDecimal.valueOf(701.002));

        Page<Price> pricePage = new PageImpl<>(List.of(price));
        given(priceRepository.findAllByFetchedAtBetween(from, to,
                PageRequest.of(pageNumber, size)))
                .willReturn(pricePage);

        PagedPriceResponse response = priceService
                .getAllByFetchedAt(from, to, pageNumber, size);

        assertEquals(1, response.items().size());
        assertEquals(price.getPrice(), response.items().getFirst().price());
        assertEquals(price.getSymbol(), response.items().getFirst().symbol());
        assertEquals(pricePage.getNumber(), response.page());
        assertEquals(pricePage.getSize(), response.size());
        assertEquals(pricePage.getTotalElements(), response.totalElements());
        assertEquals(pricePage.hasNext(), response.hasNext());

        verify(priceRepository).findAllByFetchedAtBetween(
                from, to, PageRequest.of(pageNumber, size)
        );
    }
}
