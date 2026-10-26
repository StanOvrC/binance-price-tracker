package com.shokal.binancepricetracker.repository;

import com.shokal.binancepricetracker.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findFirstByOrderByFetchedAtDesc();

    @Query("SELECT CAST(AVG(p.price) AS BIGDECIMAL) FROM Price p WHERE p.fetchedAt BETWEEN :from AND :to")
    Optional<BigDecimal> findAllByFetchedAtFromTo(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
