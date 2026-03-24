package com.marvelwargame.model.abilities;

import com.marvelwargame.model.world.Champion;
import com.marvelwargame.model.world.Damageable;
import java.util.List;

/** Restores HP to all resolved targets. */
public class HealingAbility extends Ability {

    private int healAmount;

    public HealingAbility(String name, int manaCost, int baseCooldown, int castRange,
                          AreaOfEffect castArea, int actionsRequired, int healAmount) {
        super(name, manaCost, baseCooldown, castRange, castArea, actionsRequired);
        this.healAmount = healAmount;
    }

    public int getHealAmount()        { return healAmount; }
    public void setHealAmount(int v)  { this.healAmount = v; }

    @Override
    public void execute(List<Damageable> targets) throws CloneNotSupportedException {
        for (Damageable d : targets) {
            d.setCurrentHP(d.getCurrentHP() + healAmount);
        }
    }
}
