package com.fulvis.domain.order;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class OrderStateMachine {

    private static final Map<TransitionKey, OrderTransitionRule> RULES = Map.of(
            new TransitionKey(OrderStatus.MANAGED, OrderEvent.START_PREPARATION),
            new OrderTransitionRule(OrderStatus.MANAGED, OrderEvent.START_PREPARATION, OrderStatus.IN_PREPARATION, StockAction.NONE),

            new TransitionKey(OrderStatus.IN_PREPARATION, OrderEvent.MARK_SHIPPED),
            new OrderTransitionRule(OrderStatus.IN_PREPARATION, OrderEvent.MARK_SHIPPED, OrderStatus.SHIPPED, StockAction.DECREMENT),

            new TransitionKey(OrderStatus.SHIPPED, OrderEvent.CONFIRM_RECEPTION),
            new OrderTransitionRule(OrderStatus.SHIPPED, OrderEvent.CONFIRM_RECEPTION, OrderStatus.RECEIVED, StockAction.NONE),

            new TransitionKey(OrderStatus.RECEIVED, OrderEvent.CLOSE),
            new OrderTransitionRule(OrderStatus.RECEIVED, OrderEvent.CLOSE, OrderStatus.CLOSED, StockAction.NONE),

            new TransitionKey(OrderStatus.MANAGED, OrderEvent.CANCEL),
            new OrderTransitionRule(OrderStatus.MANAGED, OrderEvent.CANCEL, OrderStatus.CANCELLED, StockAction.RELEASE),

            new TransitionKey(OrderStatus.IN_PREPARATION, OrderEvent.CANCEL),
            new OrderTransitionRule(OrderStatus.IN_PREPARATION, OrderEvent.CANCEL, OrderStatus.CANCELLED, StockAction.RELEASE)
    );

    private static final Map<OrderEvent, OrderStatus> DIRECT_TARGET_BY_EVENT = Map.of(
            OrderEvent.START_PREPARATION, OrderStatus.IN_PREPARATION,
            OrderEvent.MARK_SHIPPED, OrderStatus.SHIPPED,
            OrderEvent.CONFIRM_RECEPTION, OrderStatus.RECEIVED,
            OrderEvent.CLOSE, OrderStatus.CLOSED,
            OrderEvent.CANCEL, OrderStatus.CANCELLED
    );

    public OrderTransitionResolution resolve(OrderStatus currentStatus, OrderEvent event) {
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        Objects.requireNonNull(event, "event must not be null");

        OrderTransitionRule rule = RULES.get(new TransitionKey(currentStatus, event));
        if (rule != null) {
            return OrderTransitionResolution.applicable(rule);
        }

        if (DIRECT_TARGET_BY_EVENT.get(event) == currentStatus) {
            return OrderTransitionResolution.alreadySatisfied();
        }

        return OrderTransitionResolution.invalid();
    }

    private record TransitionKey(OrderStatus status, OrderEvent event) {
    }

    public record OrderTransitionResolution(Type type, OrderTransitionRule rule) {

        public OrderTransitionResolution {
            Objects.requireNonNull(type, "type must not be null");
            if (type == Type.APPLICABLE && rule == null) {
                throw new IllegalArgumentException("APPLICABLE resolution must contain a rule");
            }
            if (type != Type.APPLICABLE && rule != null) {
                throw new IllegalArgumentException(type + " resolution must not contain a rule");
            }
        }

        public static OrderTransitionResolution applicable(OrderTransitionRule rule) {
            return new OrderTransitionResolution(Type.APPLICABLE, Objects.requireNonNull(rule, "rule must not be null"));
        }

        public static OrderTransitionResolution alreadySatisfied() {
            return new OrderTransitionResolution(Type.ALREADY_SATISFIED, null);
        }

        public static OrderTransitionResolution invalid() {
            return new OrderTransitionResolution(Type.INVALID, null);
        }

        public Optional<OrderTransitionRule> ruleOptional() {
            return Optional.ofNullable(rule);
        }

        public enum Type {
            APPLICABLE,
            ALREADY_SATISFIED,
            INVALID
        }
    }
}
