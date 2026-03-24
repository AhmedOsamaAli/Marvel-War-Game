package model.effects;

import model.abilities.AreaOfEffect;
import model.abilities.DamagingAbility;
import model.world.Champion;

public class Embrace extends Effect {

	public Embrace(int d) {
		super("Embrace", d, EffectType.BUFF);
	}
	
	
	public void apply(Champion c) {
		c.setCurrentHP((int) (c.getMaxHP()*0.2)+c.getCurrentHP());
		c.setMana((int) (c.getMana()*1.2));
		c.setSpeed((int) (c.getSpeed()*1.2));
		c.setAttackDamage((int) (c.getAttackDamage()*1.2));

	}
	
	public void remove(Champion c) {
		c.getAppliedEffects().remove(this);

		c.setSpeed((int) (c.getSpeed() / 1.2 ));
		c.setAttackDamage((int) (c.getAttackDamage() / 1.2 ));

	}


}
