package com.marvelwargame.model.world;

import com.marvelwargame.model.effects.Effect;
import java.awt.Point;

/**
 * Destructible obstacle on the board. Provides cover but has limited HP.
 */
public class Cover implements Damageable {

    private static final int MIN_HP = 100;
    private static final int HP_RANGE = 900;

    private int currentHP;
    private final Point location;

    public Cover(int x, int y) {
        this.location = new Point(x, y);
        this.currentHP = (int) (Math.random() * HP_RANGE) + MIN_HP;
    }

    @Override
    public int getCurrentHP() {
        return currentHP;
    }

    @Override
    public void setCurrentHP(int hp) {
        this.currentHP = Math.max(0, hp);
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public void useEffect(Effect e) {
        // Covers are not affected by effects
    }

    public boolean isDestroyed() {
        return currentHP <= 0;
    }
}
