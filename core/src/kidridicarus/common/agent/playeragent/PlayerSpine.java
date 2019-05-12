package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.scrollkillbox.ScrollKillBox;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;

public class PlayerSpine extends SolidContactSpine {
	public PlayerSpine(PlayerAgentBody body) {
		super(body);
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
		SolidTiledMapAgent ctMap = agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapPointSolid(position); 
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapAgent ctMap = agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}

	public boolean isContactScrollKillBox() {
		return agentSensor.getFirstContactByClass(ScrollKillBox.class) != null;
	}

	/*
	 * The argument is named minWalkVelocity, and not maxStandVelocity, because minWalkVelocity is used
	 * by the player classes already.
	 */
	public boolean isStandingStill(float minWalkVelocity) {
		return (body.getVelocity().x > -minWalkVelocity && body.getVelocity().x < minWalkVelocity);
	}
}
