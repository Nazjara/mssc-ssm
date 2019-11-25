package com.nazjara.config;

import com.nazjara.model.PaymentEvent;
import com.nazjara.model.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import static com.nazjara.service.PaymentServiceImpl.PAYMENT_ID_HEADER;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();

        stateMachine.start();

        assertSame(stateMachine.getState().getId(), PaymentState.NEW);

        stateMachine.sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH)
                .setHeader(PAYMENT_ID_HEADER, "paymentId")
                .build());

        assertSame(stateMachine.getState().getId(), PaymentState.PRE_AUTH);

        stateMachine.sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH)
                .setHeader(PAYMENT_ID_HEADER, "paymentId")
                .build());

        assertSame(stateMachine.getState().getId(), PaymentState.AUTH_ERROR);
    }
}