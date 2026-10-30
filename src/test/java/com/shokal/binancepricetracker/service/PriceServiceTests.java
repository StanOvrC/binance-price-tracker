package com.shokal.binancepricetracker.service;

import com.shokal.binancepricetracker.BinanceClient;
import com.shokal.binancepricetracker.dto.BinancePriceResponse;
import com.shokal.binancepricetracker.entity.Price;
import com.shokal.binancepricetracker.repository.PriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
