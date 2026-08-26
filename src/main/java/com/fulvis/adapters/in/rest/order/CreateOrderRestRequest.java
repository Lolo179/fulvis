package com.fulvis.adapters.in.rest.order;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record CreateOrderRestRequest(List<CreateOrderLineRestRequest> lines) {

    @JsonAnySetter
    public void rejectUnknownProperty(String propertyName, Object value) {
        throw new IllegalArgumentException("Unknown property: " + propertyName);
    }
}
