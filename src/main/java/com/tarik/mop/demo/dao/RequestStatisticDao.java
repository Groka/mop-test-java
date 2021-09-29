package com.tarik.mop.demo.dao;

import com.tarik.mop.demo.entity.RequestStatistic;
import com.tarik.mop.demo.model.RequestStatisticResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestStatisticDao extends CrudRepository<RequestStatistic, Long> {
    @Query(value = "SELECT new com.tarik.mop.demo.model.RequestStatisticResult(rs.url, AVG(rs.responseTime)) FROM RequestStatistic rs WHERE DATE(rs.requestExecutedAt) >= CURRENT_DATE GROUP BY rs.url")
    List<RequestStatisticResult> getAvgResponseTimePerUrl();
}
