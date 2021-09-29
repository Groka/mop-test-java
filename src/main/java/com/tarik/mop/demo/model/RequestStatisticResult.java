package com.tarik.mop.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestStatisticResult {
    private String url;
    @JsonProperty("average_response_time")
    private Double averageResponseTime;
}
