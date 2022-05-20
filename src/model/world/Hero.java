package model.world;

import java.util.ArrayList;

import model.effects.Effect;
import model.effects.EffectType;
import model.effects.Embrace;

public class Hero extends Champion {

	public Hero(String name, int maxHP, int mana, int maxActionsPerTurn, int speed, int attackRange, int attackDamage) {
		super(name, maxHP, mana, maxActionsPerTurn, speed, attackRange, attackDamage);
	}

	
	
	
	public void useLeaderAbility(ArrayList<Champion> targets) {
		for(int i=0;i<targets.size();i++) {
			ArrayList<Effect>CE=targets.get(i).getAppliedEffects();
			for(int j=0;j<CE.size();j++) {
				if(CE.get(j).getType().equals(EffectType.DEBUFF)) {
					CE.remove(j);
					
					j--;
				}
			}
				Embrace effect=new Embrace(2); 
				targets.get(i).getAppliedEffects().add(new Embrace(2));
				effect.apply(targets.get(i));
			
			}
			
		}
		
	}





	
	

