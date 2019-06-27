package kidridicarus.common.info;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Quick Common Convenient - convenience class/methods
public class QCC {
	/*
	 * Returns 0 or a positive value.
	 * Used to check that the time passed to animation's getKeyFrame is positive, even when the time is
	 * running backwards.
	 */
	public static float ensurePositive(float original, float delta) {
		if(original >= 0f)
			return original;

		if(delta == 0f)
			return 0f;
		return (float) (original + (-Math.floor(original / delta))*delta);
	}

	public static Rectangle rectSizePos(Vector2 size, Vector2 pos) {
		return new Rectangle(pos.x-size.x/2f, pos.y-size.y/2f, size.x, size.y);
	}
}
