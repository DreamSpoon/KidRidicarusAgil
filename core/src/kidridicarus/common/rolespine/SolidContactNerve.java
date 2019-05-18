package kidridicarus.common.rolespine;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.common.rolesensor.SolidContactSensor;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.Role;

public class SolidContactNerve {
	private SolidContactSensor solidSensor = null;

	public SolidContactSensor createSolidContactSensor(Role role) {
		solidSensor = new SolidContactSensor(role);
		return solidSensor;
	}

	public boolean isDirSolid(Direction4 dir, Rectangle bounds) {
		if(solidSensor == null) {
			return false;
		}
		return solidSensor.isDirSolid(dir, bounds);
	}

	// convenience method
	public boolean isOnGround(Rectangle bounds) {
		return isDirSolid(Direction4.DOWN, bounds);
	}

	public boolean isOnCeiling(Rectangle bounds) {
		return isDirSolid(Direction4.UP, bounds);
	}
}
