package com.fulvis.adapters.in.rest.item;

import com.fulvis.adapters.in.rest.error.RestExceptionHandler;
import com.fulvis.application.item.stock.GetItemStockResult;
import com.fulvis.application.item.stock.GetItemStockUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ItemRestControllerTest {

    private static final String TRACE_ID = "trace-123";

    private GetItemStockUseCase getItemStockUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        getItemStockUseCase = mock(GetItemStockUseCase.class);
        mockMvc = standaloneSetup(new ItemRestController(getItemStockUseCase))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void givenItemIdWhenGetStockThenItDelegatesToUseCase() throws Exception {
        when(getItemStockUseCase.execute("item-123"))
                .thenReturn(new GetItemStockResult("item-123", 10, 2, 8));

        mockMvc.perform(get("/items/item-123/stock")
                        .header("X-Trace-Id", TRACE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value("item-123"))
                .andExpect(jsonPath("$.stockTotal").value(10))
                .andExpect(jsonPath("$.stockReserved").value(2))
                .andExpect(jsonPath("$.stockAvailable").value(8));

        verify(getItemStockUseCase).execute("item-123");
    }

    @Test
    void givenMissingTraceIdWhenGetStockThenItRejectsBeforeUseCase() throws Exception {
        mockMvc.perform(get("/items/item-123/stock"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MissingTraceId"))
                .andExpect(jsonPath("$.traceId").value("missing"));

        verifyNoInteractions(getItemStockUseCase);
    }
}
