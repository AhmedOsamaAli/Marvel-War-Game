package com.marvelwargame.model.world;

import com.marvelwargame.exceptions.UnallowedMovementException;
import com.marvelwargame.model.abilities.Ability;
import com.marvelwargame.model.effects.Effect;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for all playable champions.
 * Implements Comparable for turn-order priority (higher speed = lower priority value = acts first).
 */
public abstract class Champion implements Comparable<Champion>, Damageable {

    private final String name;
    private int mana;
    private final int attackRange;
    private int attackDamage;
    private int speed;
    private final int maxHP;
    private int currentHP;
    private int maxActionPointsPerTurn;   // mutable – Shock/SpeedUp change it
    private int currentActionPoints;
    private final List<Ability> abilities;
    private final List<Effect> appliedEffects;
    private Point location;
    private Condition condition;

    protected Champion(String name, int maxHP, int mana, int maxActionsPerTurn,
                       int speed, int attackRange, int attackDamage) {
        this.name = name;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.mana = mana;
        this.maxActionPointsPerTurn = maxActionsPerTurn;
        this.currentActionPoints = maxActionsPerTurn;
        this.speed = speed;
        this.attackRange = attackRange;
        this.attackDamage = attackDamage;
        this.abilities = new ArrayList<>();
        this.appliedEffects = new ArrayList<>();
        this.condition = Condition.ACTIVE;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName()                    { return name; }
    public int getMana()                       { return mana; }
    public int getAttackRange()                { return attackRange; }
    public int getAttackDamage()               { return attackDamage; }
    public int getSpeed()                      { return speed; }
    public int getMaxHP()                      { return maxHP; }
    @Override public int getCurrentHP()        { return currentHP; }
    public int getMaxActionPointsPerTurn()     { return maxActionPointsPerTurn; }
    public int getCurrentActionPoints()        { return currentActionPoints; }
    public List<Ability> getAbilities()        { return abilities; }
    public List<Effect> getAppliedEffects()    { return appliedEffects; }
    @Override public Point getLocation()       { return location; }
    public Condition getCondition()            { return condition; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setMana(int mana)              { this.mana = Math.max(0, mana); }
    public void setAttackDamage(int v)         { this.attackDamage = v; }
    public void setSpeed(int v)                { this.speed = v; }
    public void setCondition(Condition c)      { this.condition = c; }
    public void setCurrentActionPoints(int v)  { this.currentActionPoints = v; }

    public void setMaxActionPointsPerTurn(int v) {
        this.maxActionPointsPerTurn = v;
    }

    @Override
    public void setCurrentHP(int hp) {
        this.currentHP = Math.max(0, Math.min(maxHP, hp));
    }

    public void setLocation(Point p) throws UnallowedMovementException {
        if (condition == Condition.INACTIVE || condition == Condition.ROOTED) {
            throw new UnallowedMovementException();
        }
        this.location = p;
    }

    /** Force-set location without condition check (used during board placement). */
    public void placeAt(Point p) {
        this.location = p;
    }

    @Override
    public void useEffect(Effect e) {
        appliedEffects.add(e);
    }

    /**
     * Reduce cooldowns of all abilities by 1 at end of turn.
     * Also decrements all active effect durations and removes expired ones.
     */
    public void endTurn() {
        for (Ability a : abilities) {
            if (a.getCurrentCooldown() > 0) {
                a.setCurrentCooldown(a.getCurrentCooldown() - 1);
            }
        }

        List<Effect> expired = new ArrayList<>();
        for (Effect e : new ArrayList<>(appliedEffects)) {
            e.setDuration(e.getDuration() - 1);
            if (e.getDuration() <= 0) {
                expired.add(e);
            }
        }
        for (Effect e : expired) {
            e.remove(this);
        }
    }

    /** Reset action points at the start of this champion's turn. */
    public void startTurn() {
        this.currentActionPoints = maxActionPointsPerTurn;
        setMana(mana + maxActionPointsPerTurn * 10);
    }

    /** Comparable by speed (descending – highest speed acts first). */
    @Override
    public int compareTo(Champion other) {
        return Integer.compare(other.speed, this.speed);
    }

    /** Abstract: each champion type implements its leader ability differently. */
    public abstract void useLeaderAbility(List<Champion> targets);

    @Override
    public String toString() {
        return String.format("%s [HP:%d/%d | Mana:%d | AP:%d | SPD:%d]",
                name, currentHP, maxHP, mana, currentActionPoints, speed);
    }
}
