package com.hl.eventstream;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

@FunctionalInterface
public interface EventConsumer {
    Event consume(Event event);

    default Observable<Event> consume(Event event, Scheduler scheduler) {
        return Observable
                .fromCallable(() -> this.consume(event))
                .subscribeOn(scheduler);
    }
}