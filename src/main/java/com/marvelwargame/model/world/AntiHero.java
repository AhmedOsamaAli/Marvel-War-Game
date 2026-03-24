package com.marvelwargame.model.world;

import com.marvelwargame.model.effects.Dodge;
import java.util.List;

/**
 * AntiHero champion. Leader ability: applies permanent Dodge (for 3 turns) to all allies.
 */
public class AntiHero extends Champion {

    public AntiHero(String name, int maxHP, int mana, int maxActionsPerTurn,
                    int speed, int attackRange, int attackDamage) {
        super(name, maxHP, mana, maxActionsPerTurn, speed, attackRange, attackDamage);
    }

    @Override
    public void useLeaderAbility(List<Champion> targets) {
        for (Champion c : targets) {
            Dodge dodge = new Dodge(3);
            dodge.apply(c);
            c.getAppliedEffects().add(dodge);
        }
    }
}
