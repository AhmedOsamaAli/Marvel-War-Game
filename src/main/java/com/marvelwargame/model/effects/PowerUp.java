package com.marvelwargame.model.effects;

import com.marvelwargame.model.abilities.Ability;
import com.marvelwargame.model.abilities.DamagingAbility;
import com.marvelwargame.model.abilities.HealingAbility;
import com.marvelwargame.model.world.Champion;

/** BUFF – boosts all ability damage/heal amounts by 20%. */
public class PowerUp extends Effect {

    public PowerUp(int duration) { super("PowerUp", duration, EffectType.BUFF); }

    @Override
    public void apply(Champion c) {
        for (Ability a : c.getAbilities()) {
            if (a instanceof DamagingAbility da) {
                da.setDamageAmount((int)(da.getDamageAmount() * 1.2));
            } else if (a instanceof HealingAbility ha) {
                ha.setHealAmount((int)(ha.getHealAmount() * 1.2));
            }
        }
    }

    @Override
    public void remove(Champion c) {
        for (Ability a : c.getAbilities()) {
            if (a instanceof DamagingAbility da) {
                da.setDamageAmount((int)Math.ceil(da.getDamageAmount() / 1.2));
            } else if (a instanceof HealingAbility ha) {
                ha.setHealAmount((int)Math.ceil(ha.getHealAmount() / 1.2));
            }
        }
        c.getAppliedEffects().remove(this);
    }
}
