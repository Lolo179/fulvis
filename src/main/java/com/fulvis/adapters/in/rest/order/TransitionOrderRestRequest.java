package com.fulvis.adapters.in.rest.order;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fulvis.domain.order.OrderEvent;

@JsonIgnoreProperties(ignoreUnknown = false)
public record TransitionOrderRestRequest(OrderEvent event) {

    @JsonAnySetter
    public void rejectUnknownProperty(String propertyName, Object value) {
        throw new IllegalArgumentException("Unknown property: " + propertyName);
    }
}
