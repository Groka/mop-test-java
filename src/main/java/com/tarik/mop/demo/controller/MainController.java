package com.tarik.mop.demo.controller;

import com.tarik.mop.demo.entity.RequestStatistic;
import com.tarik.mop.demo.model.ProductInfoResult;
import com.tarik.mop.demo.model.RequestStatisticResult;
import com.tarik.mop.demo.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class MainController {
    
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping(value = "/test")
    public Map<String, Object> getTest() {
        Map<String, Object> ret = new HashMap<>();
        List<ProductInfoResult> data = statisticsService.getProductsWithPrices();
        ret.put("size", data.size());
        ret.put("data", data);
        return ret;
    }

    @GetMapping(value = "/stats")
    public List<RequestStatisticResult> getRequestsStatistics() {
        return statisticsService.getRequestsStatistics();
    }
}
