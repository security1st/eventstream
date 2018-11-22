package com.hl.eventstream;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Hl Inc.
 * Copyright (C) 2004-2018 All Rights Reserved.
 *
 * @author HuangLiang
 * @date 18-8-31
 */
@Slf4j
public class WaitOnConcurrentModification implements EventConsumer {

    private final ConcurrentMap<Integer, Lock> clientLocks = new ConcurrentHashMap<>();
    private final EventConsumer downstream;
    private final Timer lockWait;

    public WaitOnConcurrentModification(EventConsumer downstream, MetricRegistry metricRegistry) {
        this.downstream = downstream;
        lockWait = metricRegistry.timer(MetricRegistry.name(WaitOnConcurrentModification.class, "lockWait"));
    }

    @Override
    public Event consume(Event event) {
        try {
            final Lock lock = findClientLock(event);
            final Timer.Context time = lockWait.time();
            try {
                final boolean locked = lock.tryLock(1, TimeUnit.SECONDS);
                time.stop();
                if(locked) {
                    downstream.consume(event);
                }
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
        }
        return event;
    }

    private Lock findClientLock(Event event) {
        return clientLocks.computeIfAbsent(
                event.getClientId(),
                clientId -> new ReentrantLock());
    }

}
