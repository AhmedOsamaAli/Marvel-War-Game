package com.marvelwargame.model.world;

import com.marvelwargame.model.effects.Effect;
import java.awt.Point;

/**
 * Represents anything on the board that can receive and deal damage.
 */
public interface Damageable {
    int getCurrentHP();
    void setCurrentHP(int hp);
    Point getLocation();
    void useEffect(Effect e);
}
