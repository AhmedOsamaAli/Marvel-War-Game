package com.marvelwargame.model.abilities;

import com.marvelwargame.model.effects.Effect;
import com.marvelwargame.model.world.Champion;
import com.marvelwargame.model.world.Damageable;
import java.util.List;

/** Applies a crowd-control Effect to all resolved targets. */
public class CrowdControlAbility extends Ability {

    private final Effect effect;

    public CrowdControlAbility(String name, int manaCost, int baseCooldown, int castRange,
                               AreaOfEffect castArea, int actionsRequired, Effect effect) {
        super(name, manaCost, baseCooldown, castRange, castArea, actionsRequired);
        this.effect = effect;
    }

    public Effect getEffect() { return effect; }

    @Override
    public void execute(List<Damageable> targets) throws CloneNotSupportedException {
        for (Damageable d : targets) {
            if (d instanceof Champion c) {
                Effect clone = (Effect) effect.clone();
                clone.apply(c);
                c.useEffect(clone);
            }
        }
    }
}
