package com.tarik.mop.demo.callable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarik.mop.demo.dao.RequestStatisticDao;
import com.tarik.mop.demo.entity.RequestStatistic;
import com.tarik.mop.demo.model.ProductInfo;
import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProductCallable implements Callable<List<ProductInfo>> {

    private final Integer apiNumber;
    private final RequestStatisticDao requestStatisticDao;

    public ProductCallable(Integer apiNumber, RequestStatisticDao requestStatisticDao) {
        this.apiNumber = apiNumber;
        this.requestStatisticDao = requestStatisticDao;
    }

    @Override
    public List<ProductInfo> call() {
        List<ProductInfo> result = new ArrayList<>();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            System.out.println("Calling https://simple-scala-api.herokuapp.com/api" + apiNumber + " --- Thread: " + Thread.currentThread());

            HttpGet request = new HttpGet("https://simple-scala-api.herokuapp.com/api" + apiNumber);

            Thread executingThread = Thread.currentThread();
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            Future<?> cancelationTask = executorService.submit(() -> {
                while (!executingThread.isInterrupted());
                if (!request.isAborted()) {
                    request.abort();
                }
            });

            Instant requestStartTime = Instant.now();

            CloseableHttpResponse response = httpClient.execute(request);

            cancelationTask.cancel(true);
            executorService.shutdown();

            if (response.getStatusLine().getStatusCode() == 200) {
                Instant requestEndTime = Instant.now();

                var bufReader = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                var builder = new StringBuilder();

                String line;

                while ((line = bufReader.readLine()) != null) {
                    builder.append(line);
                }

                result = List.of(new ObjectMapper().readValue(builder.toString(), ProductInfo[].class));

                RequestStatistic requestStatistic = new RequestStatistic();
                requestStatistic.setUrl("https://simple-scala-api.herokuapp.com/api" + apiNumber);
                requestStatistic.setRequestExecutedAt(Date.from(requestStartTime));
                requestStatistic.setResponseTime(Duration.between(requestStartTime, requestEndTime).toMillis());
                requestStatisticDao.save(requestStatistic);

                System.out.println("Result size: " + result.size());
            }
        } catch (SocketException | ConnectionClosedException e) {
            System.out.println("Connection closed on thread: " + Thread.currentThread());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
//        return Arrays.asList(result.getStatusCode() == HttpStatus.OK ? Objects.requireNonNull(result.getBody()) : new ProductInfo[] {});
    }
}
