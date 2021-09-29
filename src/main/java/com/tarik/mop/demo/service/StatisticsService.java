package com.tarik.mop.demo.service;

import com.tarik.mop.demo.entity.RequestStatistic;
import com.tarik.mop.demo.model.ProductInfoResult;
import com.tarik.mop.demo.model.RequestStatisticResult;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface StatisticsService {

    List<ProductInfoResult> getProductsWithPrices();

    List<RequestStatisticResult> getRequestsStatistics();
}
