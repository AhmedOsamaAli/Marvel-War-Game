package model.world;

import java.util.ArrayList;

import model.effects.Effect;
import model.effects.EffectType;
import model.effects.Embrace;
import model.effects.Stun;
import engine.Game.*;

public class AntiHero extends Champion {

	public AntiHero(String name, int maxHP, int mana, int maxActionsPerTurn, int speed, int attackRange,
			int attackDamage) {
		super(name, maxHP, mana, maxActionsPerTurn, speed, attackRange, attackDamage);
	}

	
	public void useLeaderAbility(ArrayList<Champion> targets) {
		for (int i = 0; i < targets.size(); i++) {
			
			Stun effect=new Stun(2); 
			targets.get(i).getAppliedEffects().add(effect);
			effect.apply(targets.get(i));
		}

	}


}
