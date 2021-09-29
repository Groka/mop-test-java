package com.tarik.mop.demo.impl;

import com.tarik.mop.demo.callable.ProductCallable;
import com.tarik.mop.demo.dao.RequestStatisticDao;
import com.tarik.mop.demo.model.ProductInfo;
import com.tarik.mop.demo.model.ProductInfoResult;
import com.tarik.mop.demo.model.RequestStatisticResult;
import com.tarik.mop.demo.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RequestStatisticDao requestStatisticDao;

    @Override
    public List<ProductInfoResult> getProductsWithPrices() {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        List<Future<List<ProductInfo>>> futureList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Future<List<ProductInfo>> future = executorService.submit(new ProductCallable(i + 1, requestStatisticDao));
            futureList.add(future);
        }

        List<Future<List<ProductInfo>>> waitingFutures = new ArrayList<>(futureList);

        AtomicReference<List<ProductInfoResult>> result = new AtomicReference<>();
        
        futureList.parallelStream().
                map(future -> {
                    try {
                        long start = System.currentTimeMillis();
                        List<ProductInfo> ret = future.get();
                        waitingFutures.remove(future);

                        if (waitingFutures.size() == 1) {
                            waitingFutures.forEach(f -> f.cancel(true));
                        }

                        long end = System.currentTimeMillis();
                        System.out.println("Time spent waiting for the response:  " + (end - start) + " miliseconds");

                        return ret;
                    } catch (InterruptedException | ExecutionException | CancellationException e) {
                        System.out.println("Future interrupted because: " + e.getMessage());
                    }
                    return new ArrayList<ProductInfo>();
                })
                .reduce((productInfos, productInfos2) -> {
                    long start = System.currentTimeMillis();

                    final var ret = Stream.concat(productInfos.parallelStream(), productInfos2.stream()).collect(Collectors.toList());

                    long end = System.currentTimeMillis();
                    System.out.println("Time spent in reduce:  " + (end - start) + " miliseconds");

                    return ret;
                })
                .ifPresent(productInfos -> {
                    long start = System.currentTimeMillis();
                            result.set(
                                    productInfos.parallelStream()
                                            .collect(Collectors.groupingByConcurrent(ProductInfo::getProductId, Collectors.mapping(ProductInfo::getPrice, Collectors.toList())))
                                            .entrySet().parallelStream().sorted(Comparator.comparingInt(Map.Entry::getKey))
                                            .map(integerListEntry -> new ProductInfoResult(integerListEntry.getKey(), integerListEntry.getValue()))
                                            .collect(Collectors.toList()));

                    long end = System.currentTimeMillis();
                    System.out.println("Time spent mapping:  " + (end - start) + " miliseconds");
                });

        executorService.shutdown();

        return result.get();
    }

    @Override
    public List<RequestStatisticResult> getRequestsStatistics() {
        return requestStatisticDao.getAvgResponseTimePerUrl();
    }
}
