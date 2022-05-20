package model.effects;
import model.world.*;
import model.abilities.*;

public class Silence extends Effect {

	public Silence(int d) {
		super("Silence", d, EffectType.DEBUFF);
	}

	public void apply(Champion c) {
		 c.setLastCondition();
		c.setMaxActionPointsPerTurn(c.getMaxActionPointsPerTurn()+2);
		c.setCurrentActionPoints(c.getCurrentActionPoints()+2);
	}

	@Override
	public void remove(Champion c) {
		
		c.setCondition(c.getLastCondition());
		c.setCurrentActionPoints(c.getCurrentActionPoints()-2);
		c.setMaxActionPointsPerTurn( c.getMaxActionPointsPerTurn()-2);
		c.getAppliedEffects().remove(this);

	}

}
