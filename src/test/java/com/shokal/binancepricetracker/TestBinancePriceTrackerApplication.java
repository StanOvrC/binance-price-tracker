package com.shokal.binancepricetracker;

import org.springframework.boot.SpringApplication;

public class TestBinancePriceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.from(BinancePriceTrackerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
