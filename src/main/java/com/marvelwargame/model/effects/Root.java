package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;
import com.marvelwargame.model.world.Condition;

/** DEBUFF – champion is rooted (cannot move). */
public class Root extends Effect {

    public Root(int duration) { super("Root", duration, EffectType.DEBUFF); }

    @Override
    public void apply(Champion c) {
        if (c.getCondition() == Condition.ACTIVE) {
            c.setCondition(Condition.ROOTED);
        }
    }

    @Override
    public void remove(Champion c) {
        c.getAppliedEffects().remove(this);
        boolean stillStunned = c.getAppliedEffects().stream()
                .anyMatch(e -> e.getName().equals("Stun"));
        boolean stillRooted  = c.getAppliedEffects().stream()
                .anyMatch(e -> e.getName().equals("Root"));
        if (!stillStunned && !stillRooted) {
            c.setCondition(Condition.ACTIVE);
        } else if (stillStunned) {
            c.setCondition(Condition.INACTIVE);
        } else {
            c.setCondition(Condition.ROOTED);
        }
    }
}
