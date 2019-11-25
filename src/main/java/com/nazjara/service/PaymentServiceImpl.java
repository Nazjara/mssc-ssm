package com.nazjara.service;

import com.nazjara.model.Payment;
import com.nazjara.model.PaymentEvent;
import com.nazjara.model.PaymentState;
import com.nazjara.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    @Transactional
    public Payment create(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTH);

        return stateMachine;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.AUTH);

        return stateMachine;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> declinePreAuthorization(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTH_DECLINED);

        return stateMachine;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> declineAuthorization(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.AUTH_DECLINED);

        return stateMachine;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent paymentEvent) {
        Message<PaymentEvent> message = MessageBuilder.withPayload(paymentEvent)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        stateMachine.sendEvent(message);
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
        Payment payment = paymentRepository.getOne(paymentId);

        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(String.valueOf(paymentId));
        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(stateMachineAccessor -> {
                    stateMachineAccessor.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    stateMachineAccessor.resetStateMachine(
                            new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        stateMachine.start();

        return stateMachine;
    }
}