package com.marvelwargame.model.world;

import java.util.List;

/**
 * Villain champion. Leader ability: knocks out any enemy below 30% HP.
 */
public class Villain extends Champion {

    public Villain(String name, int maxHP, int mana, int maxActionsPerTurn,
                   int speed, int attackRange, int attackDamage) {
        super(name, maxHP, mana, maxActionsPerTurn, speed, attackRange, attackDamage);
    }

    @Override
    public void useLeaderAbility(List<Champion> targets) {
        for (Champion c : targets) {
            if ((double) c.getCurrentHP() / c.getMaxHP() < 0.3) {
                c.setCurrentHP(0);
                c.setCondition(Condition.KNOCKEDOUT);
            }
        }
    }
}
