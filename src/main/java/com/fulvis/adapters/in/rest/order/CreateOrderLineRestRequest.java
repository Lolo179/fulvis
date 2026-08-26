package com.fulvis.adapters.in.rest.order;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public record CreateOrderLineRestRequest(String itemId, int quantity) {

    @JsonAnySetter
    public void rejectUnknownProperty(String propertyName, Object value) {
        throw new IllegalArgumentException("Unknown property: " + propertyName);
    }
}
