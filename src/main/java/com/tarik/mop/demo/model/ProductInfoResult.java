package com.tarik.mop.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductInfoResult {
    Integer productId;
    List<Double> prices;
}
