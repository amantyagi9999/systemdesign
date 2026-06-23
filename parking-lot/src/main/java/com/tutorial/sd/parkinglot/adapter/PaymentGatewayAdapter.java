package com.tutorial.sd.parkinglot.adapter;

import java.util.UUID;

public interface PaymentGatewayAdapter {
    boolean pay(UUID ticketId, double amount);
}