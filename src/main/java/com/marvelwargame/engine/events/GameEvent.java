package com.marvelwargame.engine.events;

import com.marvelwargame.model.world.Champion;

/**
 * Immutable game event record, fired through the EventBus to notify observers.
 */
public record GameEvent(
        Type type,
        Champion source,
        Champion target,
        Object payload,
        String message
) {
    public enum Type {
        CHAMPION_MOVED,
        CHAMPION_ATTACKED,
        ABILITY_CAST,
        CHAMPION_DAMAGED,
        CHAMPION_KNOCKED_OUT,
        CHAMPION_TURN_STARTED,
        COVER_DESTROYED,
        LEADER_ABILITY_USED,
        GAME_OVER
    }

    public static GameEvent of(Type type, String message) {
        return new GameEvent(type, null, null, null, message);
    }

    public static GameEvent of(Type type, Champion source, String message) {
        return new GameEvent(type, source, null, null, message);
    }

    public static GameEvent of(Type type, Champion source, Champion target, String message) {
        return new GameEvent(type, source, target, null, message);
    }

    public static GameEvent of(Type type, Champion source, Object payload, String message) {
        return new GameEvent(type, source, null, payload, message);
    }
}
