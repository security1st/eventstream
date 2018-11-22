package com.hl.eventstream.eventstream;

/**
 * Hl Inc.
 * Copyright (C) 2004-2018 All Rights Reserved.
 *
 * @author HuangLiang
 * @date 18-8-24
 */
import com.codahale.metrics.MetricRegistry;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) throws Exception {
        MetricRegistry metricRegistry =	new MetricRegistry();
        ProjectionMetrics metrics = new ProjectionMetrics(metricRegistry);
        ClientProjection clientProjection = new ClientProjection(metrics);

        EventStream es = new EventStream();
        Disposable disposable = es.consume(clientProjection);

        /*NaivePool naivePool = new NaivePool(10, clientProjection, metricRegistry);
        EventStream es = new EventStream();
        Disposable disposable = es.consume(naivePool);*/


        /*FailOnConcurrentModification concurrentModification = new FailOnConcurrentModification(clientProjection);
        SmartPool smartPool =  new SmartPool(0x10, concurrentModification, metricRegistry);
        IgnoreDuplicates withoutDuplicates =  new IgnoreDuplicates(smartPool, metricRegistry);
        EventStream es = new EventStream();
        Disposable disposable = es.consume(withoutDuplicates);*/

        System.in.read();
    }


}