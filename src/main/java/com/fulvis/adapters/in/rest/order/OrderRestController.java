package com.fulvis.adapters.in.rest.order;

import com.fulvis.adapters.in.rest.error.TraceIdValidator;
import com.fulvis.application.order.create.CreateOrderCommand;
import com.fulvis.application.order.create.CreateOrderLineCommand;
import com.fulvis.application.order.create.CreateOrderResult;
import com.fulvis.application.order.create.CreateOrderUseCase;
import com.fulvis.application.order.get.GetOrderHistoryResult;
import com.fulvis.application.order.get.GetOrderLineResult;
import com.fulvis.application.order.get.GetOrderResult;
import com.fulvis.application.order.get.GetOrderUseCase;
import com.fulvis.application.order.transition.TransitionOrderCommand;
import com.fulvis.application.order.transition.TransitionOrderResult;
import com.fulvis.application.order.transition.TransitionOrderUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/orders")
public class OrderRestController {

    private final CreateOrderUseCase createOrderUseCase;
    private final TransitionOrderUseCase transitionOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public OrderRestController(
            CreateOrderUseCase createOrderUseCase,
            TransitionOrderUseCase transitionOrderUseCase,
            GetOrderUseCase getOrderUseCase
    ) {
        this.createOrderUseCase = Objects.requireNonNull(createOrderUseCase, "createOrderUseCase must not be null");
        this.transitionOrderUseCase = Objects.requireNonNull(transitionOrderUseCase, "transitionOrderUseCase must not be null");
        this.getOrderUseCase = Objects.requireNonNull(getOrderUseCase, "getOrderUseCase must not be null");
    }

    @PostMapping
    public ResponseEntity<CreateOrderRestResponse> createOrder(
            @RequestHeader(value = TraceIdValidator.TRACE_ID_HEADER, required = false) String traceId,
            @RequestBody CreateOrderRestRequest request
    ) {
        TraceIdValidator.requireTraceId(traceId);

        CreateOrderResult result = createOrderUseCase.execute(toCommand(request));

        return ResponseEntity
                .created(URI.create("/orders/" + result.orderId()))
                .body(new CreateOrderRestResponse(result.orderId(), result.status(), result.createdAt()));
    }

    @PostMapping("/{orderId}/transitions")
    public ResponseEntity<TransitionOrderRestResponse> transitionOrder(
            @RequestHeader(value = TraceIdValidator.TRACE_ID_HEADER, required = false) String traceId,
            @PathVariable String orderId,
            @RequestBody TransitionOrderRestRequest request
    ) {
        TraceIdValidator.requireTraceId(traceId);
        if (request.event() == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        TransitionOrderResult result = transitionOrderUseCase.execute(new TransitionOrderCommand(orderId, request.event()));

        return ResponseEntity.ok(new TransitionOrderRestResponse(result.orderId(), result.status(), result.occurredAt()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderRestResponse> getOrder(
            @RequestHeader(value = TraceIdValidator.TRACE_ID_HEADER, required = false) String traceId,
            @PathVariable String orderId
    ) {
        TraceIdValidator.requireTraceId(traceId);

        GetOrderResult result = getOrderUseCase.execute(orderId);

        return ResponseEntity.ok(toResponse(result));
    }

    private static CreateOrderCommand toCommand(CreateOrderRestRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (request.lines() == null) {
            throw new IllegalArgumentException("lines must not be null");
        }
        return new CreateOrderCommand(request.lines().stream()
                .map(line -> new CreateOrderLineCommand(line.itemId(), line.quantity()))
                .toList());
    }

    private static GetOrderRestResponse toResponse(GetOrderResult result) {
        return new GetOrderRestResponse(
                result.orderId(),
                result.status(),
                result.createdAt(),
                result.updatedAt(),
                result.lines().stream()
                        .map(OrderRestController::toResponse)
                        .toList(),
                result.history().stream()
                        .map(OrderRestController::toResponse)
                        .toList()
        );
    }

    private static GetOrderLineRestResponse toResponse(GetOrderLineResult result) {
        return new GetOrderLineRestResponse(result.itemId(), result.quantity());
    }

    private static GetOrderHistoryRestResponse toResponse(GetOrderHistoryResult result) {
        return new GetOrderHistoryRestResponse(result.previousStatus(), result.newStatus(), result.timestamp());
    }
}
