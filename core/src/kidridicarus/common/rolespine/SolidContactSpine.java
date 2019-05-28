package kidridicarus.common.rolespine;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.common.rolesensor.SolidContactSensor;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.Role;

public class SolidContactSpine extends BasicRoleSpine {
	private SolidContactNerve scNerve;

	public SolidContactSpine(Role parentRole) {
		super(parentRole);
		scNerve = new SolidContactNerve();
	}

	public SolidContactSensor createSolidContactSensor() {
		return scNerve.createSolidContactSensor(parentRole);
	}

	public boolean isOnGround() {
		return scNerve.isOnGround(roleBody.getBounds());
	}

	public boolean isOnCeiling() {
		return scNerve.isOnCeiling(roleBody.getBounds());
	}

	public boolean isSideMoveBlocked(boolean isRight) {
		return (isRight && scNerve.isDirSolid(Direction4.RIGHT, roleBody.getBounds())) ||
				(!isRight && scNerve.isDirSolid(Direction4.LEFT, roleBody.getBounds()));
	}

	/*
	 * Use current contacts and given velocity, which may vary from AgentBody's velocity, to determine if movement
	 * along the direction given by velocity is blocked by a solid.
	 * Returns true if blocked by solid, otherwise returns false.
	 */
	public boolean isMoveBlocked(Vector2 velocity) {
		Direction4 horizontalMove = Direction4.NONE;
		Direction4 verticalMove = Direction4.NONE;
		if(velocity.x > UInfo.VEL_EPSILON)
			horizontalMove = Direction4.RIGHT;
		else if(velocity.x < -UInfo.VEL_EPSILON)
			horizontalMove = Direction4.LEFT;
		if(velocity.y > UInfo.VEL_EPSILON)
			verticalMove = Direction4.UP;
		else if(velocity.y < -UInfo.VEL_EPSILON)
			verticalMove = Direction4.DOWN;
		// if move is blocked in either direction then return true, otherwise return false
		return isMoveBlocked(horizontalMove) || isMoveBlocked(verticalMove);
	}

	public boolean isMoveBlocked(Direction4 moveDir) {
		// if no move direction then block is impossible, so return false
		if(moveDir == Direction4.NONE)
			return false;
		return scNerve.isDirSolid(moveDir, roleBody.getBounds());
	}
}
