package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;

class PitArrowBrain {
	private static final float LIVE_TIME = 0.217f;
	private static final float IMMEDIATE_EXPIRE_TIME = 1/30f;
	private static final float GIVE_DAMAGE = 1f;

	// the player is the Agent that gives damage, the arrow is just a go-between
	private Pit playerParent;
	// hooks are used to remove arrow Agent, not player Agent
	private AgentHooks arrowParentHooks;
	private SolidContactSpine spine;
	private float moveStateTimer;
	private boolean isExpireImmediate;
	private Direction4 arrowDir;
	private RoomBox lastKnownRoom;
	private boolean despawnMe;

	PitArrowBrain(Pit playerParent, AgentHooks arrowParentHooks, SolidContactSpine spine, boolean isExpireImmediate,
			Direction4 arrowDir) {
		this.playerParent = playerParent;
		this.arrowParentHooks = arrowParentHooks;
		this.spine = spine;
		this.arrowDir = arrowDir;
		// check the definition properties, maybe the shot needs to expire immediately
		this.isExpireImmediate = isExpireImmediate;
		moveStateTimer = 0f;
		lastKnownRoom = null;
		despawnMe = false;
	}

	void processContactFrame() {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : spine.getContactDmgTakeAgents()) {
			// do not damage player parent
			if(agent == playerParent)
				continue;
			if(agent.onTakeDamage(playerParent, GIVE_DAMAGE, spine.getPosition()))
				despawnMe = true;
		}
		// if not touching keep alive box, or if touching despawn, or if hit a solid, then set despawn flag
		if(!spine.isContactKeepAlive() || spine.isContactDespawn() || spine.isMoveBlocked(arrowDir))
			despawnMe = true;
		// otherwise update last known room if possible
		else if(spine.getCurrentRoom() != null)
			lastKnownRoom = spine.getCurrentRoom();
	}

	SpriteFrameInput processFrame(float delta) {
		moveStateTimer += delta;
		if(moveStateTimer > LIVE_TIME || (isExpireImmediate && moveStateTimer > IMMEDIATE_EXPIRE_TIME))
			despawnMe = true;
		if(despawnMe) {
			arrowParentHooks.removeThisAgent();
			return null;
		}
		// do space wrap last so that contacts are maintained
		spine.checkDoSpaceWrap(lastKnownRoom);
		return new PitArrowSpriteFrameInput(spine.getPosition(), arrowDir);
	}
}
