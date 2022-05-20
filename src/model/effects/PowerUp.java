package model.effects;

import java.util.ArrayList;

import model.abilities.Ability;
import model.abilities.*;
import model.world.Champion;

public class PowerUp extends Effect {

	public PowerUp(int d) {
		super("PowerUp", d, EffectType.BUFF);
		
	}
	public void apply(Champion c) {
		 ArrayList<Ability> abilities=c.getAbilities();
		 for(int i=0;i<abilities.size();i++) {
			 if(abilities.get(i) instanceof HealingAbility ) {
				 HealingAbility HA=(HealingAbility) abilities.get(i);
				 HA.setHealAmount((int)(HA.getHealAmount()*1.2));
				 
			 }
			 if(abilities.get(i) instanceof DamagingAbility) {
				 DamagingAbility DA=(DamagingAbility) abilities.get(i);
				 DA.setDamageAmount((int)(DA.getDamageAmount()*1.2));
			 }
		 }	
	}
	
	public void remove(Champion c) {
		
		 ArrayList<Ability> abilities=c.getAbilities();
		 for(int i=0;i<abilities.size();i++) {
			 if(abilities.get(i) instanceof HealingAbility ) {
				 HealingAbility HA=(HealingAbility) abilities.get(i);
				 HA.setHealAmount((int)Math.ceil(HA.getHealAmount()/1.2));
				 
			 }
			 if(abilities.get(i) instanceof DamagingAbility) {
				 DamagingAbility DA=(DamagingAbility) abilities.get(i);
				 DA.setDamageAmount((int)Math.ceil(DA.getDamageAmount()/1.2));
			 }
		 }
			c.getAppliedEffects().remove(this);

		 
		
	}

}
