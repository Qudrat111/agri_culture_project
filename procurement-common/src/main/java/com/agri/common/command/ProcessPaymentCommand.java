package com.agri.common.command;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
    String orderId,
    String buyerId,
    BigDecimal totalAmount
) {}
