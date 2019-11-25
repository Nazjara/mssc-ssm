package com.nazjara.service;

import com.nazjara.model.Payment;
import com.nazjara.model.PaymentEvent;
import com.nazjara.model.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment create(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declinePreAuthorization(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuthorization(Long paymentId);
}