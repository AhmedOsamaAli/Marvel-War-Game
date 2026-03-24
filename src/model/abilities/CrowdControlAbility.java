package model.abilities;

import java.util.ArrayList;

import model.effects.Effect;
import model.world.Champion;
import model.world.Damageable;

public class CrowdControlAbility extends Ability {

	private Effect effect;

	public CrowdControlAbility(String name, int manaCost, int baseCooldown, int castRange, AreaOfEffect castArea,
			int actionsRequired, Effect effect) {
		super(name, manaCost, baseCooldown, castRange, castArea, actionsRequired);
		this.effect = effect;
	}

	public Effect getEffect() {
		return effect;
	}
	public void execute(ArrayList<Damageable> targets) throws CloneNotSupportedException {
        for(int i = 0; i < targets.size();i++) {

            Champion chmp = (Champion)targets.get(i);
            Effect e = (Effect) this.effect.clone();
            e.apply((Champion) targets.get(i));
            targets.get(i).useEffect((Effect) effect.clone());

        }
    }

}
