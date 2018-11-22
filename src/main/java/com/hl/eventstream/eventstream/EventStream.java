package com.hl.eventstream.eventstream;

import com.google.common.cache.CacheBuilder;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EventStream {

    /*public void consume(EventConsumer consumer) {
        observe()
                .subscribe(
                        consumer::consume,
                        e -> log.error("Error emitting event", e)
                );
    }*/

    private Set<UUID> recentUuids() {
        return Collections.newSetFromMap(
                CacheBuilder.newBuilder()
                        .expireAfterWrite(10, TimeUnit.SECONDS)
                        .<UUID, Boolean>build()
                        .asMap()
        );
    }

    public Disposable consume(EventConsumer consumer) {
        return observe()
                .distinct(Event::getUuid, this::recentUuids)
                .groupBy(Event::getClientId)
                .flatMap(byClient -> byClient
                        .observeOn(Schedulers.io())
                        .map(consumer::consume)
                )
                .subscribe(
                        e -> {},
                        e -> log.error("Fatal error", e));
    }

    public Observable<Event> observe() {
        return Observable
                .interval(1, TimeUnit.MILLISECONDS)
                .delay(x -> Observable.timer(RandomUtils.nextInt(0, 1_000), TimeUnit.MICROSECONDS))
                .map(x -> new Event(RandomUtils.nextInt(1_000, 1_100), UUID.randomUUID()))
                .flatMap(this::occasionallyDuplicate, 100)
                .observeOn(Schedulers.io());
    }

    private Observable<Event> occasionallyDuplicate(Event x) {
        final Observable<Event> event = Observable.just(x);
        if (Math.random() >= 0.01) {
            return event;
        }
        final Observable<Event> duplicated =
                event.delay(RandomUtils.nextInt(10, 5_000), TimeUnit.MILLISECONDS);
        return event.concatWith(duplicated);
    }
}
