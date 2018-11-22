package com.hl.eventstream;



import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class Event {

    private final Instant created = Instant.now();
    private final int clientId;
    private final UUID uuid;

}
