package com.tarik.mop.demo.controller;

import com.tarik.mop.demo.model.ProductInfoResult;
import com.tarik.mop.demo.model.RequestStatisticResult;
import com.tarik.mop.demo.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MainController {
    
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping(value = "/products")
    public List<ProductInfoResult> getProductsWithPrices() {
        return statisticsService.getProductsWithPrices();
    }

    @GetMapping(value = "/stats")
    public List<RequestStatisticResult> getRequestsStatistics() {
        return statisticsService.getRequestsStatistics();
    }
}
