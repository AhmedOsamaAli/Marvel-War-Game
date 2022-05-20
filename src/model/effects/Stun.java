package model.effects;

import model.world.*;

public class Stun extends Effect {

	public Stun(int d) {
		super("Stun", d, EffectType.DEBUFF);
	}


	public void apply(Champion c) {
			c.setCondition(Condition.INACTIVE);
		
	}

	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		for(int i = 0; i < c.getAppliedEffects().size();i++) {
			if(c.getAppliedEffects().get(i).getName().equals("Stun"))
				return;
		}
		for(int i = 0; i < c.getAppliedEffects().size();i++) {
			if(c.getAppliedEffects().get(i).getName().equals("Root")) {
				c.setCondition(Condition.ROOTED);
				return;
		}
		}
		c.setCondition(Condition.ACTIVE);

	}
	
	
	

}
