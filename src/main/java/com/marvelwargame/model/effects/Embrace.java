package com.marvelwargame.model.effects;

import com.marvelwargame.model.world.Champion;

/**
 * BUFF – heals 20% maxHP, boosts mana ×1.2, speed ×1.2, attack ×1.2.
 * Hero leader ability applies this.
 */
public class Embrace extends Effect {

    public Embrace(int duration) { super("Embrace", duration, EffectType.BUFF); }

    @Override
    public void apply(Champion c) {
        c.setCurrentHP(c.getCurrentHP() + (int)(c.getMaxHP() * 0.2));
        c.setMana((int)(c.getMana() * 1.2));
        c.setSpeed((int)(c.getSpeed() * 1.2));
        c.setAttackDamage((int)(c.getAttackDamage() * 1.2));
    }

    @Override
    public void remove(Champion c) {
        c.setSpeed((int)(c.getSpeed() / 1.2));
        c.setAttackDamage((int)(c.getAttackDamage() / 1.2));
        c.getAppliedEffects().remove(this);
    }
}
