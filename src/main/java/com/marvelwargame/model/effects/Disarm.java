package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;
import com.marvelwargame.model.world.Condition;

/** DEBUFF – champion cannot attack. */
public class Disarm extends Effect {
    public Disarm(int duration) { super("Disarm", duration, EffectType.DEBUFF); }

    @Override public void apply(Champion c) { /* flag checked in Game.attack */ }

    @Override
    public void remove(Champion c) {
        c.getAppliedEffects().remove(this);
    }
}
