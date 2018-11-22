package com.hl.eventstream.eventstream;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Hl Inc.
 * Copyright (C) 2004-2018 All Rights Reserved.
 *
 * @author HuangLiang
 * @date 18-8-31
 */
public class SmartPool implements EventConsumer, Closeable {

    private final List<LinkedBlockingQueue<Runnable>> queues;
    private final List<ExecutorService> threadPools;
    private final EventConsumer downstream;

    public SmartPool(int size, EventConsumer downstream, MetricRegistry metricRegistry) {
        this.downstream = downstream;
        this.queues = IntStream
                .range(0, size)
                .mapToObj(i -> new LinkedBlockingQueue<Runnable>())
                .collect(Collectors.toList());
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
        List<ThreadPoolExecutor> list = queues
                .stream()
                .map(q -> new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, q, threadFactory))
                .collect(Collectors.toList());
        this.threadPools = new CopyOnWriteArrayList<>(list);
        metricRegistry.register(MetricRegistry.name(ProjectionMetrics.class, "queue"), (Gauge<Double>) this::averageQueueLength);
    }

    private double averageQueueLength() {
        double totalLength =
                queues
                        .stream()
                        .mapToDouble(LinkedBlockingQueue::size)
                        .sum();
        return totalLength / queues.size();
    }

    @Override
    public void close() {
        threadPools.forEach(ExecutorService::shutdown);
    }

    @Override
    public Event consume(Event event) {
        final int threadIdx = event.getClientId() & (threadPools.size() - 1);
        final ExecutorService executor = threadPools.get(threadIdx);
        executor.submit(() -> downstream.consume(event));
        return event;
    }
}
