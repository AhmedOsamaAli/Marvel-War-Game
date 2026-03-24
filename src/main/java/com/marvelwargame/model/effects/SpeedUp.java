package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;

/** BUFF – speed ×1.15 and +1 action point per turn. */
public class SpeedUp extends Effect {

    public SpeedUp(int duration) { super("SpeedUp", duration, EffectType.BUFF); }

    @Override
    public void apply(Champion c) {
        c.setSpeed((int)(c.getSpeed() * 1.15));
        c.setMaxActionPointsPerTurn(c.getMaxActionPointsPerTurn() + 1);
        c.setCurrentActionPoints(c.getCurrentActionPoints() + 1);
    }

    @Override
    public void remove(Champion c) {
        c.setSpeed((int)(c.getSpeed() / 1.15));
        c.setMaxActionPointsPerTurn(c.getMaxActionPointsPerTurn() - 1);
        c.setCurrentActionPoints(c.getCurrentActionPoints() - 1);
        c.getAppliedEffects().remove(this);
    }
}
