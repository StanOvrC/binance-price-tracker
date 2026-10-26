package com.shokal.binancepricetracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Getter
@Setter
@Table(name = "price")
public class Price {
    public Price() {
        this.fetchedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "symbol")
    private String symbol;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "fetched_at")
    private OffsetDateTime fetchedAt;
}
