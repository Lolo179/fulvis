package com.fulvis.adapters.in.rest.order;

import com.fulvis.adapters.in.rest.error.RestExceptionHandler;
import com.fulvis.application.order.create.CreateOrderCommand;
import com.fulvis.application.order.create.CreateOrderResult;
import com.fulvis.application.order.create.CreateOrderUseCase;
import com.fulvis.application.order.get.GetOrderHistoryResult;
import com.fulvis.application.order.get.GetOrderLineResult;
import com.fulvis.application.order.get.GetOrderResult;
import com.fulvis.application.order.get.GetOrderUseCase;
import com.fulvis.application.order.transition.TransitionOrderCommand;
import com.fulvis.application.order.transition.TransitionOrderResult;
import com.fulvis.application.order.transition.TransitionOrderUseCase;
import com.fulvis.domain.order.InvalidOrderTransitionException;
import com.fulvis.domain.order.OrderEvent;
import com.fulvis.domain.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class OrderRestControllerTest {

    private static final String TRACE_ID = "trace-123";

    private CreateOrderUseCase createOrderUseCase;
    private TransitionOrderUseCase transitionOrderUseCase;
    private GetOrderUseCase getOrderUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createOrderUseCase = mock(CreateOrderUseCase.class);
        transitionOrderUseCase = mock(TransitionOrderUseCase.class);
        getOrderUseCase = mock(GetOrderUseCase.class);
        mockMvc = standaloneSetup(new OrderRestController(createOrderUseCase, transitionOrderUseCase, getOrderUseCase))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void givenCreateOrderRequestWhenPostOrdersThenItDelegatesToUseCase() throws Exception {
        when(createOrderUseCase.execute(any()))
                .thenReturn(new CreateOrderResult("order-123", OrderStatus.MANAGED, Instant.parse("2026-08-18T10:15:30Z")));

        mockMvc.perform(post("/orders")
                        .header("X-Trace-Id", TRACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "itemId": "item-123",
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/order-123"))
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.status").value("MANAGED"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-18T10:15:30Z"));

        ArgumentCaptor<CreateOrderCommand> commandCaptor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().lines()).hasSize(1);
        assertThat(commandCaptor.getValue().lines().getFirst().itemId()).isEqualTo("item-123");
        assertThat(commandCaptor.getValue().lines().getFirst().quantity()).isEqualTo(2);
    }

    @Test
    void givenMissingTraceIdWhenPostOrdersThenItRejectsBeforeUseCase() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "itemId": "item-123",
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MissingTraceId"))
                .andExpect(jsonPath("$.traceId").value("missing"));

        verifyNoInteractions(createOrderUseCase, transitionOrderUseCase, getOrderUseCase);
    }

    @Test
    void givenTransitionEventWhenPostTransitionThenItDelegatesWithoutExposingIdempotent() throws Exception {
        when(transitionOrderUseCase.execute(any()))
                .thenReturn(new TransitionOrderResult("order-123", OrderStatus.SHIPPED, Instant.parse("2026-08-18T10:20:00Z"), true));

        mockMvc.perform(post("/orders/order-123/transitions")
                        .header("X-Trace-Id", TRACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "event": "MARK_SHIPPED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.occurredAt").value("2026-08-18T10:20:00Z"))
                .andExpect(jsonPath("$.idempotent").doesNotExist());

        ArgumentCaptor<TransitionOrderCommand> commandCaptor = ArgumentCaptor.forClass(TransitionOrderCommand.class);
        verify(transitionOrderUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().orderId()).isEqualTo("order-123");
        assertThat(commandCaptor.getValue().event()).isEqualTo(OrderEvent.MARK_SHIPPED);
    }

    @Test
    void givenTransitionRequestWithTargetStatusWhenPostTransitionThenItRejectsContract() throws Exception {
        mockMvc.perform(post("/orders/order-123/transitions")
                        .header("X-Trace-Id", TRACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SHIPPED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("InvalidOrderLines"))
                .andExpect(jsonPath("$.traceId").value(TRACE_ID));

        verifyNoInteractions(createOrderUseCase, transitionOrderUseCase, getOrderUseCase);
    }

    @Test
    void givenTransitionRequestWithEventAndTargetStatusWhenPostTransitionThenItRejectsContract() throws Exception {
        mockMvc.perform(post("/orders/order-123/transitions")
                        .header("X-Trace-Id", TRACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "event": "MARK_SHIPPED",
                                  "status": "SHIPPED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("InvalidOrderLines"))
                .andExpect(jsonPath("$.traceId").value(TRACE_ID));

        verifyNoInteractions(createOrderUseCase, transitionOrderUseCase, getOrderUseCase);
    }

    @Test
    void givenBusinessErrorWhenPostTransitionThenItMapsErrorWithTraceId() throws Exception {
        when(transitionOrderUseCase.execute(any()))
                .thenThrow(new InvalidOrderTransitionException("invalid transition"));

        mockMvc.perform(post("/orders/order-123/transitions")
                        .header("X-Trace-Id", TRACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "event": "MARK_SHIPPED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("InvalidOrderTransition"))
                .andExpect(jsonPath("$.traceId").value(TRACE_ID));
    }

    @Test
    void givenOrderIdWhenGetOrderThenItReturnsLinesAndHistory() throws Exception {
        when(getOrderUseCase.execute("order-123"))
                .thenReturn(new GetOrderResult(
                        "order-123",
                        OrderStatus.IN_PREPARATION,
                        Instant.parse("2026-08-18T10:15:30Z"),
                        Instant.parse("2026-08-18T10:17:00Z"),
                        List.of(new GetOrderLineResult("item-123", 2)),
                        List.of(new GetOrderHistoryResult(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, Instant.parse("2026-08-18T10:17:00Z")))
                ));

        mockMvc.perform(get("/orders/order-123")
                        .header("X-Trace-Id", TRACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"))
                .andExpect(jsonPath("$.lines[0].itemId").value("item-123"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.history[0].previousStatus").value("MANAGED"))
                .andExpect(jsonPath("$.history[0].newStatus").value("IN_PREPARATION"));

        verify(getOrderUseCase).execute("order-123");
    }
}
