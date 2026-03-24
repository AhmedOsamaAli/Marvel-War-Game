package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;
import com.marvelwargame.model.world.Condition;

/** DEBUFF – champion is stunned (INACTIVE; cannot act). */
public class Stun extends Effect {

    public Stun(int duration) { super("Stun", duration, EffectType.DEBUFF); }

    @Override
    public void apply(Champion c) {
        c.setCondition(Condition.INACTIVE);
    }

    @Override
    public void remove(Champion c) {
        c.getAppliedEffects().remove(this);
        boolean stillStunned = c.getAppliedEffects().stream()
                .anyMatch(e -> e.getName().equals("Stun"));
        boolean stillRooted  = c.getAppliedEffects().stream()
                .anyMatch(e -> e.getName().equals("Root"));
        if (stillStunned) {
            c.setCondition(Condition.INACTIVE);
        } else if (stillRooted) {
            c.setCondition(Condition.ROOTED);
        } else {
            c.setCondition(Condition.ACTIVE);
        }
    }
}
