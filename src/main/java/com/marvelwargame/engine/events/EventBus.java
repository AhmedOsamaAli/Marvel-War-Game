package com.marvelwargame.engine.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple synchronous publish-subscribe event bus.
 * The Game posts events; UI controllers subscribe to react.
 */
public final class EventBus {

    private final Map<GameEvent.Type, List<Consumer<GameEvent>>> listeners =
            new EnumMap<>(GameEvent.Type.class);

    /** Subscribe a handler for a specific event type. */
    public void subscribe(GameEvent.Type type, Consumer<GameEvent> handler) {
        listeners.computeIfAbsent(type, t -> new ArrayList<>()).add(handler);
    }

    /** Unsubscribe all handlers for a type. */
    public void clearSubscriptions(GameEvent.Type type) {
        listeners.remove(type);
    }

    /** Clear all subscribers (e.g. when starting a new game). */
    public void clearAll() {
        listeners.clear();
    }

    /** Publish an event to all registered handlers. */
    public void publish(GameEvent event) {
        List<Consumer<GameEvent>> handlers = listeners.get(event.type());
        if (handlers != null) {
            for (Consumer<GameEvent> h : List.copyOf(handlers)) {
                h.accept(event);
            }
        }
    }
}
