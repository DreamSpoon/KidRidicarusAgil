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
			body.applyImpulse(new Vector2(amt, 0f));
		else
			body.applyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Ensure horizontal velocity is within -max to +max.
	 */
	private void capHorizontalVelocity(float max) {
		if(body.getVelocity().x > max)
			body.setVelocity(max, body.getVelocity().y);
		else if(body.getVelocity().x < -max)
			body.setVelocity(-max, body.getVelocity().y);
	}

	protected void applyHorizImpulseAndCapVel(boolean moveRight, float xImpulse, float maxXvel) {
		applyHorizontalImpulse(moveRight, xImpulse);
		capHorizontalVelocity(maxXvel);
	}

	// maxVelocity must be positive because it is multiplied by -1 in the logic
	protected void capFallVelocity(float maxVelocity) {
		if(body.getVelocity().y < -maxVelocity)
			body.setVelocity(body.getVelocity().x, -maxVelocity);
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
		return (body.getVelocity().x > -minWalkVelocity && body.getVelocity().x < minWalkVelocity);
	}
}
