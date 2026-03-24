package model.effects;

import model.abilities.*;

import model.world.*;

public class Root extends Effect {
	
	public Root(int d) {
		super("Root", d, EffectType.DEBUFF);
	}

	public void apply(Champion c) {
		 c.setLastCondition();
		 if(!c.getLastCondition().equals(Condition.INACTIVE)) {
			 c.setCondition(Condition.ROOTED);	 
		 }
		
	}

	
	public void remove(Champion c) {
		
	c.getAppliedEffects().remove(this);
		
		
	}

}
