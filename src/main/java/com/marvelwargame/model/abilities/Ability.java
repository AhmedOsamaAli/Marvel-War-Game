package com.marvelwargame.model.abilities;

import com.marvelwargame.model.world.Damageable;
import java.util.List;

/**
 * Abstract base for all abilities. Strategy pattern – execute() is unique per subclass.
 */
public abstract class Ability {

    private final String name;
    private final int manaCost;
    private final int requiredActionPoints;
    private final int castRange;
    private final int baseCooldown;
    private int currentCooldown;
    private final AreaOfEffect castArea;

    protected Ability(String name, int manaCost, int baseCooldown, int castRange,
                      AreaOfEffect castArea, int actionsRequired) {
        this.name = name;
        this.manaCost = manaCost;
        this.baseCooldown = baseCooldown;
        this.castRange = castRange;
        this.castArea = castArea;
        this.requiredActionPoints = actionsRequired;
        this.currentCooldown = 0;
    }

    public String getName()               { return name; }
    public int getManaCost()              { return manaCost; }
    public int getRequiredActionPoints()  { return requiredActionPoints; }
    public int getCastRange()             { return castRange; }
    public int getBaseCooldown()          { return baseCooldown; }
    public int getCurrentCooldown()       { return currentCooldown; }
    public AreaOfEffect getCastArea()     { return castArea; }

    public void setCurrentCooldown(int cd) {
        this.currentCooldown = Math.min(cd, baseCooldown);
    }

    public boolean isReady()  { return currentCooldown == 0; }
    public boolean onCooldown() { return currentCooldown > 0; }

    /** Execute this ability on the resolved list of targets. */
    public abstract void execute(List<Damageable> targets) throws CloneNotSupportedException;

    @Override
    public String toString() {
        return String.format("%s [Mana:%d|AP:%d|CD:%d/%d|Range:%d|%s]",
                name, manaCost, requiredActionPoints, currentCooldown, baseCooldown, castRange, castArea);
    }
}
