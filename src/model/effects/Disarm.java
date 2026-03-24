package model.effects;

import model.abilities.AreaOfEffect;
import model.abilities.DamagingAbility;
import model.world.Champion;

public class Disarm extends Effect {

	public Disarm(int d) {
		super("Disarm", d, EffectType.DEBUFF);
	}


	public void apply(Champion c) {
		 DamagingAbility Punch = new DamagingAbility("Punch",0,1,1,AreaOfEffect.SINGLETARGET,1,50);
		 c.AddAbility(Punch);
		
	}


	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);
		for(int i=0;i<c.getAbilities().size();i++)
		{
			if(c.getAbilities().get(i).getName().equals("Punch"))
			{
				c.getAbilities().remove(i);
				break;
			}
		}
		
	}


}
