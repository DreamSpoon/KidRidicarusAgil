package kidridicarus.common.role.playerrole;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.metarole.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.role.scrollkillbox.ScrollKillBox;
import kidridicarus.common.rolespine.SolidContactSpine;
import kidridicarus.story.Role;

public class PlayerSpine extends SolidContactSpine {
	public PlayerSpine(Role parentRole) {
		super(parentRole);
	}

	protected void applyHorizontalImpulse(boolean moveRight, float amt) {
		if(moveRight)
			roleBody.applyImpulse(new Vector2(amt, 0f));
		else
			roleBody.applyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Ensure horizontal velocity is within -max to +max.
	 */
	private void capHorizontalVelocity(float max) {
		if(roleBody.getVelocity().x > max)
			roleBody.setVelocity(max, roleBody.getVelocity().y);
		else if(roleBody.getVelocity().x < -max)
			roleBody.setVelocity(-max, roleBody.getVelocity().y);
	}

	protected void applyHorizImpulseAndCapVel(boolean moveRight, float xImpulse, float maxXvel) {
		applyHorizontalImpulse(moveRight, xImpulse);
		capHorizontalVelocity(maxXvel);
	}

	// maxVelocity must be positive because it is multiplied by -1 in the logic
	protected void capFallVelocity(float maxVelocity) {
		if(roleBody.getVelocity().y < -maxVelocity)
			roleBody.setVelocity(roleBody.getVelocity().x, -maxVelocity);
	}

	public boolean isMapPointSolid(Vector2 position) {
		SolidTiledMapRole ctMap = roleSensor.getFirstContactByUserDataClass(SolidTiledMapRole.class);
		return ctMap == null ? false : ctMap.isMapPointSolid(position); 
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapRole ctMap = roleSensor.getFirstContactByUserDataClass(SolidTiledMapRole.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}

	public boolean isContactScrollKillBox() {
		return roleSensor.getFirstContactByUserDataClass(ScrollKillBox.class) != null;
	}

	/*
	 * The argument is named minWalkVelocity, and not maxStandVelocity, because minWalkVelocity is used
	 * by the player classes already.
	 */
	public boolean isStandingStill(float minWalkVelocity) {
		return (roleBody.getVelocity().x > -minWalkVelocity && roleBody.getVelocity().x < minWalkVelocity);
	}
}
