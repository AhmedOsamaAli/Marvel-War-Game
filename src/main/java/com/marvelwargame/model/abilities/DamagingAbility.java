package com.marvelwargame.model.abilities;

import com.marvelwargame.model.effects.Effect;
import com.marvelwargame.model.effects.Shield;
import com.marvelwargame.model.world.Champion;
import com.marvelwargame.model.world.Condition;
import com.marvelwargame.model.world.Damageable;
import java.util.List;

/** Deals a fixed amount of damage to all resolved targets. */
public class DamagingAbility extends Ability {

    private int damageAmount;

    public DamagingAbility(String name, int manaCost, int baseCooldown, int castRange,
                           AreaOfEffect castArea, int actionsRequired, int damageAmount) {
        super(name, manaCost, baseCooldown, castRange, castArea, actionsRequired);
        this.damageAmount = damageAmount;
    }

    public int getDamageAmount()          { return damageAmount; }
    public void setDamageAmount(int v)    { this.damageAmount = v; }

    @Override
    public void execute(List<Damageable> targets) throws CloneNotSupportedException {
        for (Damageable d : targets) {
            if (d instanceof Champion c) {
                // Check Shield – consume it and skip damage
                boolean shielded = false;
                for (Effect e : List.copyOf(c.getAppliedEffects())) {
                    if (e instanceof Shield) {
                        e.remove(c);
                        shielded = true;
                        break;
                    }
                }
                if (shielded) continue;
            }
            d.setCurrentHP(d.getCurrentHP() - damageAmount);
            if (d instanceof Champion c && c.getCurrentHP() == 0) {
                c.setCondition(Condition.KNOCKEDOUT);
            }
        }
    }
}
