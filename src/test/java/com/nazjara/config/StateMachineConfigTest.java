package com.nazjara.config;

import com.nazjara.model.PaymentEvent;
import com.nazjara.model.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();

        stateMachine.start();

        System.out.println(stateMachine.getState().toString());

        stateMachine.sendEvent(PaymentEvent.PRE_AUTH);

        System.out.println(stateMachine.getState().toString());

        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);

        System.out.println(stateMachine.getState().toString());
    }
}