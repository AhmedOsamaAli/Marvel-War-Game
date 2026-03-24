package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;

/**
 * Abstract base for all status effects. Strategy pattern: each subclass
 * encapsulates apply/remove logic independently.
 */
public abstract class Effect implements Cloneable {

    private final String name;
    private int duration;
    private final EffectType type;

    protected Effect(String name, int duration, EffectType type) {
        this.name = name;
        this.duration = duration;
        this.type = type;
    }

    public String getName()       { return name; }
    public int getDuration()      { return duration; }
    public void setDuration(int d){ this.duration = d; }
    public EffectType getType()   { return type; }

    /** Apply this effect to target champion (modifies stats). */
    public abstract void apply(Champion c);

    /** Remove this effect from target champion (restores stats). */
    public abstract void remove(Champion c);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return name + "(" + duration + " turns)";
    }
}
