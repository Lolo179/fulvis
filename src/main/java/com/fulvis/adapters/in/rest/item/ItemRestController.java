package com.fulvis.adapters.in.rest.item;

import com.fulvis.adapters.in.rest.error.TraceIdValidator;
import com.fulvis.application.item.stock.GetItemStockResult;
import com.fulvis.application.item.stock.GetItemStockUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/items")
public class ItemRestController {

    private final GetItemStockUseCase getItemStockUseCase;

    public ItemRestController(GetItemStockUseCase getItemStockUseCase) {
        this.getItemStockUseCase = Objects.requireNonNull(getItemStockUseCase, "getItemStockUseCase must not be null");
    }

    @GetMapping("/{itemId}/stock")
    public ResponseEntity<GetItemStockRestResponse> getItemStock(
            @RequestHeader(value = TraceIdValidator.TRACE_ID_HEADER, required = false) String traceId,
            @PathVariable String itemId
    ) {
        TraceIdValidator.requireTraceId(traceId);

        GetItemStockResult result = getItemStockUseCase.execute(itemId);

        return ResponseEntity.ok(new GetItemStockRestResponse(
                result.itemId(),
                result.stockTotal(),
                result.stockReserved(),
                result.stockAvailable()
        ));
    }
}
