package model.world;

import java.util.ArrayList;

import model.effects.Effect;
import model.effects.EffectType;
import model.effects.Embrace;

public class Villain extends Champion {

	public Villain(String name, int maxHP, int mana, int maxActionsPerTurn, int speed, int attackRange,
			int attackDamage) {
		super(name, maxHP, mana, maxActionsPerTurn, speed, attackRange, attackDamage);
	}

	
	
	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i=0;i<targets.size();i++) {
			int cntHP=targets.get(i).getCurrentHP(),mxHP=targets.get(i).getMaxHP();
			
			if(((int) cntHP / (int) mxHP < 0.3)) {
				
				targets.get(i).setCondition(Condition.KNOCKEDOUT);
				targets.get(i).setCurrentHP(0);
				
				
				
				
			}	
			
		}
		
	}





}
