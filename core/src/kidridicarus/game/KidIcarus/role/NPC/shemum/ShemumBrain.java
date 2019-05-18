package kidridicarus.game.KidIcarus.role.NPC.shemum;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.role.optional.ContactDmgTakeRole;
import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.KidIcarus.KidIcarusAudio;
import kidridicarus.game.KidIcarus.role.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.role.other.vanishpoof.VanishPoof;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

class ShemumBrain {
	private static final float GIVE_DAMAGE = 1f;
	private static final int DROP_HEART_COUNT = 1;
	private static final float STRIKE_DELAY = 1/6f;
	private static final float FALL1_TIME = 0.05f;

	private enum MoveState { WALK, FALL1, FALL2, STRIKE_GROUND, DEAD }

	private Shemum parent;
	private RoleHooks parentRoleHooks;
	private ShemumSpine spine;
	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private boolean isAlive;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	ShemumBrain(Shemum parent, RoleHooks parentRoleHooks, ShemumSpine spine) {
		this.parent = parent;
		this.parentRoleHooks = parentRoleHooks;
		this.spine = spine;
		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		isAlive = true;
		despawnMe = false;
		lastKnownRoom = null;
	}

	void processContactFrame() {
		// push damage to contact damage Roles
		for(ContactDmgTakeRole role : spine.getContactDmgTakeRoles())
			role.onTakeDamage(parent, GIVE_DAMAGE, spine.getPosition());
		// if alive and not touching keep alive box, or if touching despawn, then despawn
		if(isAlive && (!spine.isContactKeepAlive() || spine.isContactDespawn()))
			despawnMe = true;
		// if not dead or despawning, and if room is known, then update last known room
		if(moveState != MoveState.DEAD && !despawnMe && spine.getCurrentRoom() != null)
			lastKnownRoom = spine.getCurrentRoom();
	}

	SpriteFrameInput processFrame(FrameTime frameTime) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parentRoleHooks.agentHooksBundle.agentHooks.removeThisAgent();
			return null;
		}

		// if move is blocked by solid then change facing dir
		if(spine.isSideMoveBlocked(isFacingRight))
			isFacingRight = !isFacingRight;

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				spine.doWalkMove(isFacingRight);
				break;
			case FALL1:
				break;
			case FALL2:
				// ensure fall straight down
				if(isMoveStateChange)
					spine.zeroVelocity(true, false);
				break;
			case STRIKE_GROUND:
				// turn to face player on first frame of ground strike
				if(isMoveStateChange) {
					if(spine.getPlayerDir().isRight())
						isFacingRight = true;
					else
						isFacingRight = false;
				}
				break;
			case DEAD:
				parentRoleHooks.storyHooks.createRole(VanishPoof.makeRP(spine.getPosition(), false));
				parentRoleHooks.storyHooks.createRole(AngelHeart.makeRP(spine.getPosition(), DROP_HEART_COUNT));
				parentRoleHooks.agentHooksBundle.agentHooks.removeThisAgent();
				parentRoleHooks.agentHooksBundle.audioHooks.getEar().playSound(
						KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		// do space wrap last so that contacts are maintained
//		spine.checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;

		return SprFrameTool.placeAnimFaceR(spine.getPosition(), frameTime, isFacingRight);
	}

	private MoveState getNextMoveState() {
		if(!isAlive || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(moveState == MoveState.WALK) {
			// if not on ground and moving down then start fall...
			if(!spine.isOnGround() && spine.isMovingInDir(Direction4.DOWN))
				return MoveState.FALL1;
			// otherwise continue walk to prevent "sticking" to vertexes of ledges
			else
				return MoveState.WALK;
		}
		else if(moveState == MoveState.FALL1 || moveState == MoveState.FALL2) {
			if(spine.isOnGround())
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
	boolean onTakeDamage(Role role) {
		// if dead already or the damage is from the same team then return no damage taken
		if(!isAlive || !(role instanceof PlayerRole))
			return false;

		isAlive = false;
		return true;
	}
}
