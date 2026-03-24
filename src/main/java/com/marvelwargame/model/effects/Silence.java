package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;

/** DEBUFF – champion cannot cast abilities. */
public class Silence extends Effect {

    public Silence(int duration) { super("Silence", duration, EffectType.DEBUFF); }

    @Override public void apply(Champion c)  { /* flag checked in Game.castAbility */ }
    @Override public void remove(Champion c) { c.getAppliedEffects().remove(this); }
}
