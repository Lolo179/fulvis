package com.fulvis.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.fulvis.domain.order.OrderStateMachine.OrderTransitionResolution.Type.ALREADY_SATISFIED;
import static com.fulvis.domain.order.OrderStateMachine.OrderTransitionResolution.Type.APPLICABLE;
import static com.fulvis.domain.order.OrderStateMachine.OrderTransitionResolution.Type.INVALID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

class OrderStateMachineTest {

    private final OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void givenManagedStatusWhenStartPreparationThenResolutionIsApplicable() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.MANAGED, OrderEvent.START_PREPARATION);

        assertThat(resolution.type()).isEqualTo(APPLICABLE);
        assertThat(resolution.rule()).isEqualTo(new OrderTransitionRule(
                OrderStatus.MANAGED,
                OrderEvent.START_PREPARATION,
                OrderStatus.IN_PREPARATION,
                StockAction.NONE
        ));
    }

    @Test
    void givenInPreparationStatusWhenMarkShippedThenResolutionRequiresDecrement() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.IN_PREPARATION, OrderEvent.MARK_SHIPPED);

        assertThat(resolution.type()).isEqualTo(APPLICABLE);
        assertThat(resolution.rule()).isEqualTo(new OrderTransitionRule(
                OrderStatus.IN_PREPARATION,
                OrderEvent.MARK_SHIPPED,
                OrderStatus.SHIPPED,
                StockAction.DECREMENT
        ));
    }

    @Test
    void givenShippedStatusWhenConfirmReceptionThenResolutionIsApplicable() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.SHIPPED, OrderEvent.CONFIRM_RECEPTION);

        assertThat(resolution.type()).isEqualTo(APPLICABLE);
        assertThat(resolution.rule()).isEqualTo(new OrderTransitionRule(
                OrderStatus.SHIPPED,
                OrderEvent.CONFIRM_RECEPTION,
                OrderStatus.RECEIVED,
                StockAction.NONE
        ));
    }

    @Test
    void givenReceivedStatusWhenCloseThenResolutionIsApplicable() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.RECEIVED, OrderEvent.CLOSE);

        assertThat(resolution.type()).isEqualTo(APPLICABLE);
        assertThat(resolution.rule()).isEqualTo(new OrderTransitionRule(
                OrderStatus.RECEIVED,
                OrderEvent.CLOSE,
                OrderStatus.CLOSED,
                StockAction.NONE
        ));
    }

    @Test
    void givenManagedStatusWhenCancelThenResolutionRequiresRelease() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.MANAGED, OrderEvent.CANCEL);

        assertThat(resolution.type()).isEqualTo(APPLICABLE);
        assertThat(resolution.rule()).isEqualTo(new OrderTransitionRule(
                OrderStatus.MANAGED,
                OrderEvent.CANCEL,
                OrderStatus.CANCELLED,
                StockAction.RELEASE
        ));
    }

    @Test
    void givenInPreparationStatusWhenCancelThenResolutionRequiresRelease() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.IN_PREPARATION, OrderEvent.CANCEL);

        assertThat(resolution.type()).isEqualTo(APPLICABLE);
        assertThat(resolution.rule()).isEqualTo(new OrderTransitionRule(
                OrderStatus.IN_PREPARATION,
                OrderEvent.CANCEL,
                OrderStatus.CANCELLED,
                StockAction.RELEASE
        ));
    }

    @Test
    void givenDirectTargetStatusWhenResolvingSameEventThenResolutionIsAlreadySatisfied() {
        assertThat(stateMachine.resolve(OrderStatus.IN_PREPARATION, OrderEvent.START_PREPARATION).type())
                .isEqualTo(ALREADY_SATISFIED);
        assertThat(stateMachine.resolve(OrderStatus.SHIPPED, OrderEvent.MARK_SHIPPED).type())
                .isEqualTo(ALREADY_SATISFIED);
        assertThat(stateMachine.resolve(OrderStatus.RECEIVED, OrderEvent.CONFIRM_RECEPTION).type())
                .isEqualTo(ALREADY_SATISFIED);
        assertThat(stateMachine.resolve(OrderStatus.CLOSED, OrderEvent.CLOSE).type())
                .isEqualTo(ALREADY_SATISFIED);
        assertThat(stateMachine.resolve(OrderStatus.CANCELLED, OrderEvent.CANCEL).type())
                .isEqualTo(ALREADY_SATISFIED);
    }

    @Test
    void givenLaterDifferentStatusWhenResolvingPreviousEventThenResolutionIsInvalid() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.SHIPPED, OrderEvent.START_PREPARATION);

        assertThat(resolution.type()).isEqualTo(INVALID);
        assertThat(resolution.rule()).isNull();
    }

    @Test
    void givenInvalidOperationalCombinationWhenResolvingThenResolutionIsInvalid() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.SHIPPED, OrderEvent.CANCEL);

        assertThat(resolution.type()).isEqualTo(INVALID);
        assertThat(resolution.rule()).isNull();
    }

    @Test
    void givenOrderCreatedEventWhenResolvingThenResolutionIsInvalid() {
        OrderStateMachine.OrderTransitionResolution resolution =
                stateMachine.resolve(OrderStatus.MANAGED, OrderEvent.ORDER_CREATED);

        assertThat(resolution.type()).isEqualTo(INVALID);
        assertThat(resolution.rule()).isNull();
    }

    @ParameterizedTest
    @MethodSource("expectedResolutionTypes")
    void givenAnyStatusAndEventWhenResolvingThenResolutionMatchesStateMachineSpec(
            OrderStatus status,
            OrderEvent event,
            OrderStateMachine.OrderTransitionResolution.Type expectedType
    ) {
        OrderStateMachine.OrderTransitionResolution resolution = stateMachine.resolve(status, event);

        assertThat(resolution.type()).isEqualTo(expectedType);
    }

    private static Stream<Arguments> expectedResolutionTypes() {
        return Stream.of(
                Arguments.of(OrderStatus.MANAGED, OrderEvent.ORDER_CREATED, INVALID),
                Arguments.of(OrderStatus.MANAGED, OrderEvent.START_PREPARATION, APPLICABLE),
                Arguments.of(OrderStatus.MANAGED, OrderEvent.MARK_SHIPPED, INVALID),
                Arguments.of(OrderStatus.MANAGED, OrderEvent.CONFIRM_RECEPTION, INVALID),
                Arguments.of(OrderStatus.MANAGED, OrderEvent.CLOSE, INVALID),
                Arguments.of(OrderStatus.MANAGED, OrderEvent.CANCEL, APPLICABLE),

                Arguments.of(OrderStatus.IN_PREPARATION, OrderEvent.ORDER_CREATED, INVALID),
                Arguments.of(OrderStatus.IN_PREPARATION, OrderEvent.START_PREPARATION, ALREADY_SATISFIED),
                Arguments.of(OrderStatus.IN_PREPARATION, OrderEvent.MARK_SHIPPED, APPLICABLE),
                Arguments.of(OrderStatus.IN_PREPARATION, OrderEvent.CONFIRM_RECEPTION, INVALID),
                Arguments.of(OrderStatus.IN_PREPARATION, OrderEvent.CLOSE, INVALID),
                Arguments.of(OrderStatus.IN_PREPARATION, OrderEvent.CANCEL, APPLICABLE),

                Arguments.of(OrderStatus.SHIPPED, OrderEvent.ORDER_CREATED, INVALID),
                Arguments.of(OrderStatus.SHIPPED, OrderEvent.START_PREPARATION, INVALID),
                Arguments.of(OrderStatus.SHIPPED, OrderEvent.MARK_SHIPPED, ALREADY_SATISFIED),
                Arguments.of(OrderStatus.SHIPPED, OrderEvent.CONFIRM_RECEPTION, APPLICABLE),
                Arguments.of(OrderStatus.SHIPPED, OrderEvent.CLOSE, INVALID),
                Arguments.of(OrderStatus.SHIPPED, OrderEvent.CANCEL, INVALID),

                Arguments.of(OrderStatus.RECEIVED, OrderEvent.ORDER_CREATED, INVALID),
                Arguments.of(OrderStatus.RECEIVED, OrderEvent.START_PREPARATION, INVALID),
                Arguments.of(OrderStatus.RECEIVED, OrderEvent.MARK_SHIPPED, INVALID),
                Arguments.of(OrderStatus.RECEIVED, OrderEvent.CONFIRM_RECEPTION, ALREADY_SATISFIED),
                Arguments.of(OrderStatus.RECEIVED, OrderEvent.CLOSE, APPLICABLE),
                Arguments.of(OrderStatus.RECEIVED, OrderEvent.CANCEL, INVALID),

                Arguments.of(OrderStatus.CLOSED, OrderEvent.ORDER_CREATED, INVALID),
                Arguments.of(OrderStatus.CLOSED, OrderEvent.START_PREPARATION, INVALID),
                Arguments.of(OrderStatus.CLOSED, OrderEvent.MARK_SHIPPED, INVALID),
                Arguments.of(OrderStatus.CLOSED, OrderEvent.CONFIRM_RECEPTION, INVALID),
                Arguments.of(OrderStatus.CLOSED, OrderEvent.CLOSE, ALREADY_SATISFIED),
                Arguments.of(OrderStatus.CLOSED, OrderEvent.CANCEL, INVALID),

                Arguments.of(OrderStatus.CANCELLED, OrderEvent.ORDER_CREATED, INVALID),
                Arguments.of(OrderStatus.CANCELLED, OrderEvent.START_PREPARATION, INVALID),
                Arguments.of(OrderStatus.CANCELLED, OrderEvent.MARK_SHIPPED, INVALID),
                Arguments.of(OrderStatus.CANCELLED, OrderEvent.CONFIRM_RECEPTION, INVALID),
                Arguments.of(OrderStatus.CANCELLED, OrderEvent.CLOSE, INVALID),
                Arguments.of(OrderStatus.CANCELLED, OrderEvent.CANCEL, ALREADY_SATISFIED)
        );
    }
}
