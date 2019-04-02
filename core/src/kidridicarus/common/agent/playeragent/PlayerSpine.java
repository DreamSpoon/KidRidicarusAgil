package kidridicarus.common.agent.playeragent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agent.scrollkillbox.ScrollKillBox;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.MobileAgentSpine;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.SMB.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;

public class PlayerSpine extends MobileAgentSpine {
	private static final float MIN_HEADBANG_VEL = 0.01f;

	private AgentContactHoldSensor tileBumpPushSensor;
	private AgentContactHoldSensor pipeWarpSensor;

	public PlayerSpine(PlayerAgentBody body) {
		super(body);
		tileBumpPushSensor = null;
		pipeWarpSensor = null;
	}

	public AgentContactHoldSensor createTileBumpPushSensor() {
		tileBumpPushSensor = new AgentContactHoldSensor(body);
		return tileBumpPushSensor;
	}

	public AgentContactHoldSensor createPipeWarpSensor() {
		pipeWarpSensor = new AgentContactHoldSensor(body);
		return pipeWarpSensor;
	}

	/*
	 * If moving up fast enough, then check tiles currently contacting head for closest tile to take a bump.
	 * Tile bump is applied if needed.
	 * Returns true if tile bump is applied. Otherwise returns false.
	 */
	public boolean checkDoHeadBump(TileBumpStrength bumpStrength) {
		// exit if not moving up fast enough in this frame or previous frame
		if(body.getVelocity().y < MIN_HEADBANG_VEL ||
				((PlayerAgentBody) body).getPrevVelocity().y < MIN_HEADBANG_VEL) {
			return false;
		}
		// create list of bumptiles, in order from closest to mario to farthest from mario
		TreeSet<TileBumpTakeAgent> closestTilesList = 
				new TreeSet<TileBumpTakeAgent>(new Comparator<TileBumpTakeAgent>() {
				@Override
				public int compare(TileBumpTakeAgent o1, TileBumpTakeAgent o2) {
					float dist1 = Math.abs(((Agent) o1).getPosition().x - body.getPosition().x);
					float dist2 = Math.abs(((Agent) o2).getPosition().x - body.getPosition().x);
					if(dist1 < dist2)
						return -1;
					else if(dist1 > dist2)
						return 1;
					return 0;
				}
			});
		for(TileBumpTakeAgent bumpTile : tileBumpPushSensor.getContactsByClass(TileBumpTakeAgent.class))
			closestTilesList.add(bumpTile);

		// iterate through sorted list of bump tiles, exiting upon successful bump
		Iterator<TileBumpTakeAgent> tileIter = closestTilesList.iterator();
		while(tileIter.hasNext()) {
			TileBumpTakeAgent bumpTile = tileIter.next();
			// did the tile "take" the bump?
			if(bumpTile.onTakeTileBump(body.getParent(), bumpStrength))
				return true;
		}

		// no head bumps
		return false;
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

	public void applyPlayerHeadBounce(float bounceVel) {
		body.setVelocity(body.getVelocity().x, 0f);
		body.applyImpulse(new Vector2(0f, bounceVel));
	}

	public void applyHeadBumpMove() {
		// if moving upward then arrest upward movement, but continue horizontal movement unimpeded  
		if(body.getVelocity().y > 0f)
			body.setVelocity(body.getVelocity().x, 0f);
	}

	public PipeWarp getEnterPipeWarp(Direction4 moveDir) {
		if(moveDir == null)
			return null;
		for(PipeWarp pw : pipeWarpSensor.getContactsByClass(PipeWarp.class)) {
			if(pw.canBodyEnterPipe(body.getBounds(), moveDir))
				return pw;
		}
		return null;
	}

	public boolean isMapPointSolid(Vector2 position) {
		SolidTiledMapAgent ctMap = agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapPointSolid(position); 
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapAgent ctMap = agentSensor.getFirstContactByClass(SolidTiledMapAgent.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}

	public boolean isGiveHeadBounceAllowed(Rectangle otherBounds) {
		// check bounds
		Vector2 myPrevPosition = ((PlayerAgentBody) body).getPrevPosition();
		float otherCenterY = otherBounds.y+otherBounds.height/2f;
		return body.getBounds().y >= otherCenterY ||
				myPrevPosition.y-body.getBounds().height/2f >= otherCenterY;
	}

	public RoomBox getCurrentRoom() {
		return agentSensor.getFirstContactByClass(RoomBox.class);
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

	public boolean isMovingUp() {
		return body.getVelocity().y > UInfo.VEL_EPSILON;
	}

	public boolean isMovingDown() {
		return body.getVelocity().y < -UInfo.VEL_EPSILON;
	}

	public boolean isMovingRight() {
		return body.getVelocity().x > UInfo.VEL_EPSILON;
	}

	public boolean isMovingLeft() {
		return body.getVelocity().x < -UInfo.VEL_EPSILON;
	}
}
