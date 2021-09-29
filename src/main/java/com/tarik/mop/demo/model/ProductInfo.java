package com.tarik.mop.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductInfo {
    Double price;

    @JsonProperty("product_id")
    Integer productId;
}
