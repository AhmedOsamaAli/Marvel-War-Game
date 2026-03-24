package com.marvelwargame.model.world;

import com.marvelwargame.model.effects.Effect;
import com.marvelwargame.model.effects.EffectType;
import com.marvelwargame.model.effects.Embrace;
import java.util.List;

/**
 * Hero champion. Leader ability: removes all debuffs from allies and applies Embrace.
 */
public class Hero extends Champion {

    public Hero(String name, int maxHP, int mana, int maxActionsPerTurn,
                int speed, int attackRange, int attackDamage) {
        super(name, maxHP, mana, maxActionsPerTurn, speed, attackRange, attackDamage);
    }

    @Override
    public void useLeaderAbility(List<Champion> targets) {
        for (Champion c : targets) {
            List<Effect> effects = c.getAppliedEffects();
            effects.removeIf(e -> e.getType() == EffectType.DEBUFF);
            Embrace embrace = new Embrace(2);
            embrace.apply(c);
            c.getAppliedEffects().add(embrace);
        }
    }
}
