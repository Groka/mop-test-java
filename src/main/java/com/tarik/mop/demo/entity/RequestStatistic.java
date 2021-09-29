package com.tarik.mop.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "requests_statistics")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class RequestStatistic implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "url")
    private String url;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "request_executed_at")
    private Date requestExecutedAt;

    @Column(name = "response_time")
    private long responseTime;
}
