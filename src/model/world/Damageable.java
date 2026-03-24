
package model.world;
import java.awt.*;

import model.effects.*;
import model.world.*;
public interface Damageable {
	Point getLocation();
	int getCurrentHP();
	void setCurrentHP(int hp);
	void useEffect(Effect e) throws CloneNotSupportedException;
}
