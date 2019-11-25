package com.nazjara.service;

import com.nazjara.model.Payment;
import com.nazjara.model.PaymentState;
import com.nazjara.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("20")).build();
    }

    @Test
    @Transactional
    void preAuth() {
        Payment savedPayment = paymentService.create(payment);

        assertSame(savedPayment.getState(), PaymentState.NEW);

        paymentService.preAuthorize(savedPayment.getId());

        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        assertSame(preAuthPayment.getState(), PaymentState.PRE_AUTH);

        paymentService.authorize(savedPayment.getId());

        Payment postAuthPayment = paymentRepository.getOne(savedPayment.getId());

        assertSame(postAuthPayment.getState(), PaymentState.AUTH_ERROR);
    }
}