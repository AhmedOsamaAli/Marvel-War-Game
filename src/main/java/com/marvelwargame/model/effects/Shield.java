package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;

/** BUFF – absorbs the next incoming hit (one-shot shield). Speed slightly increased. */
public class Shield extends Effect {

    public Shield(int duration) { super("Shield", duration, EffectType.BUFF); }

    @Override
    public void apply(Champion c) {
        c.setSpeed((int)(c.getSpeed() * 1.02));
    }

    @Override
    public void remove(Champion c) {
        c.setSpeed((int)(c.getSpeed() / 1.02));
        c.getAppliedEffects().remove(this);
    }
}
