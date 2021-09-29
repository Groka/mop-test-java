package com.tarik.mop.demo.callable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarik.mop.demo.dao.RequestStatisticDao;
import com.tarik.mop.demo.entity.RequestStatistic;
import com.tarik.mop.demo.model.ProductInfo;
import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProductCallable implements Callable<List<ProductInfo>> {

    private final Integer apiNumber;
    private final RequestStatisticDao requestStatisticDao;

    private static final String BASE_URL = "https://simple-scala-api.herokuapp.com/api";

    public ProductCallable(Integer apiNumber, RequestStatisticDao requestStatisticDao) {
        this.apiNumber = apiNumber;
        this.requestStatisticDao = requestStatisticDao;
    }

    @Override
    public List<ProductInfo> call() {
        List<ProductInfo> result = new ArrayList<>();
        String url = BASE_URL + apiNumber;
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            System.out.println("Calling " + url + " --- Thread: " + Thread.currentThread());

            HttpGet request = new HttpGet(url);

            Thread executingThread = Thread.currentThread();

            Future<?> cancelationTask = executorService.submit(() -> {
                while (!executingThread.isInterrupted());
                request.abort();
            });

            Instant requestStartTime = Instant.now();

            CloseableHttpResponse response = httpClient.execute(request);

            cancelationTask.cancel(true);


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
                requestStatistic.setUrl(url);
                requestStatistic.setRequestExecutedAt(Date.from(requestStartTime));
                requestStatistic.setResponseTime(Duration.between(requestStartTime, requestEndTime).toMillis());
                requestStatisticDao.save(requestStatistic);
            }
        } catch (SocketException | ConnectionClosedException e) {
            System.out.println(MessageFormat.format("Connection aborted for [{0}] on thread [{1}]", url, Thread.currentThread()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }


        return result;
    }
}
