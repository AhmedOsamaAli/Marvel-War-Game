package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;

/** BUFF – champion has a 50% chance to dodge attacks. */
public class Dodge extends Effect {
    public Dodge(int duration) { super("Dodge", duration, EffectType.BUFF); }

    @Override public void apply(Champion c)  { /* flag checked in Game.attack */ }
    @Override public void remove(Champion c) { c.getAppliedEffects().remove(this); }
}
