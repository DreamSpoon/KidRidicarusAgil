package kidridicarus.game.KidIcarus.agent.NPC.nettler;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.NPC.nettler.NettlerBody.NettlerBodyContactFrameOutput;
import kidridicarus.game.KidIcarus.agent.NPC.nettler.NettlerBody.NettlerBodyFrameOutput;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.agent.other.vanishpoof.VanishPoof;
import kidridicarus.game.info.KidIcarusAudio;

public class NettlerCharacter {
	private static final float GIVE_DAMAGE = 1f;
	private static final int DROP_HEART_COUNT = 1;
	private static final float STRIKE_DELAY = 1/6f;
	private static final float FALL1_TIME = 0.05f;

	private enum MoveState { WALK, FALL1, FALL2, STRIKE_GROUND, DEAD }

	private Nettler parent;
	private NettlerBody body;
	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public class NettlerCharacterFrameOutput {
		public float timeDelta;
		public Vector2 position;
		public boolean visible;
		public boolean isFacingRight;
		public NettlerCharacterFrameOutput(float timeDelta, Vector2 position, boolean visible,
				boolean isFacingRight) {
			this.timeDelta = timeDelta;
			this.position = position;
			this.visible = visible;
			this.isFacingRight = isFacingRight;
		}
	}

	public NettlerCharacter(Nettler parent, NettlerBody body) {
		this.parent = parent;
		this.body = body;
		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		isDead = false;
		despawnMe = false;
		lastKnownRoom = null;
	}

	public void processContactFrame(NettlerBodyContactFrameOutput bodyContactFrame) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : bodyContactFrame.contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
	}

	public NettlerCharacterFrameOutput processFrame(NettlerBodyFrameOutput bodyFrame) {
		processContacts(bodyFrame);
		processMove(bodyFrame.timeDelta);
		// draw if not despawned and not dead
		return new NettlerCharacterFrameOutput(bodyFrame.timeDelta, body.getPosition(), !despawnMe && !isDead,
				isFacingRight);
	}

	private void processContacts(NettlerBodyFrameOutput bodyFrame) {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !bodyFrame.isContactKeepAlive) || bodyFrame.isContactDespawn)
			despawnMe = true;
		// update last known room if not dead
		if(moveState != MoveState.DEAD && bodyFrame.roomBox != null)
			lastKnownRoom = bodyFrame.roomBox;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return;
		}

		// if move is blocked by solid then change facing dir
		if(body.getSpine().isSideMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL1:
				break;
			case FALL2:
				// ensure fall straight down
				if(isMoveStateChange)
					body.zeroVelocity(true, false);
				break;
			case STRIKE_GROUND:
				// turn to face player on first frame of ground strike
				if(isMoveStateChange) {
					if(body.getSpine().getPlayerDir().isRight())
						isFacingRight = true;
					else
						isFacingRight = false;
				}
				break;
			case DEAD:
				parent.getAgency().createAgent(VanishPoof.makeAP(body.getPosition(), false));
				parent.getAgency().createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
				parent.getAgency().removeAgent(parent);
				parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(moveState == MoveState.WALK) {
			// if not on ground and moving down then start fall...
			if(!body.getSpine().isOnGround() && body.getSpine().isMovingInDir(Direction4.DOWN))
				return MoveState.FALL1;
			// otherwise continue walk to prevent "sticking" to vertexes of ledges
			else
				return MoveState.WALK;
		}
		else if(moveState == MoveState.FALL1 || moveState == MoveState.FALL2) {
			if(body.getSpine().isOnGround())
				return MoveState.STRIKE_GROUND;
			else {
				if(moveState == MoveState.FALL2 || moveStateTimer > FALL1_TIME)
					return MoveState.FALL2;
				else
					return MoveState.FALL1;
			}
		}
		// STRIKE_GROUND
		else {
			if(moveStateTimer > STRIKE_DELAY)
				return MoveState.WALK;
			else
				return MoveState.STRIKE_GROUND;
		}
	}

	// assume any amount of damage kills, for now...
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}

	public void onTakeBump(Agent agent) {
		isDead = true;
	}
}
