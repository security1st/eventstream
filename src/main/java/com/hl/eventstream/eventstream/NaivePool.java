package com.hl.eventstream.eventstream;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class NaivePool implements EventConsumer, Closeable {

    private final EventConsumer downstream;
    private final ExecutorService executorService;

    public NaivePool(int size, EventConsumer downstream, MetricRegistry metricRegistry) {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        String name = MetricRegistry.name(ProjectionMetrics.class, "queue");
        Gauge<Integer> gauge = queue::size;
        metricRegistry.register(name, gauge);
        this.executorService = newFixedThreadPool(10);
        /*this.executorService =
                new ThreadPoolExecutor(
                        size, size, 0L, TimeUnit.MILLISECONDS, queue);*/
        this.downstream = downstream;
    }

    @Override
    public Event consume(Event event) {
        executorService.submit(() -> downstream.consume(event));
        return event;
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
    }
}
