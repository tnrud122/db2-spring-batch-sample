package com.example.batch.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TxnRecord(
        long id,
        String customerNm,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
