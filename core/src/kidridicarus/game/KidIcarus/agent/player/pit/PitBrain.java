package kidridicarus.game.KidIcarus.agent.player.pit;

import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.game.KidIcarus.KidIcarusAudio;
import kidridicarus.game.KidIcarus.KidIcarusPow;
import kidridicarus.game.KidIcarus.agent.player.pitarrow.PitArrow;

class PitBrain {
	private static final int MAX_HEARTS_COLLECTED = 999;
	private static final int MAX_HEALTH = 7;
	private static final int START_HEALTH = 7;
	private static final float NO_DAMAGE_TIME = 2f;
	private static final float PRE_JUMP_TIME = 0.1f;
	private static final float JUMPUP_FORCE_TIME = 0.5f;
	private static final float SHOOT_COOLDOWN = 0.3f;
	private static final float SHOT_VEL = 2f;
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.VectorP2M(4, 0);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.VectorP2M(1, 5);
	private static final float DEAD_DELAY_TIME = 3f;
	private static final Vector2 SHOT_OFFSET_HEAD_IN_TILE = UInfo.VectorP2M(0, 4);
	private static final Vector2 DUCK_POS_OFFSET = UInfo.VectorP2M(0, 4);

	enum MoveState {
		STAND, AIMUP, WALK, DUCK, PRE_JUMP, PRE_JUMP_DUCK, PRE_JUMP_AIMUP, JUMP, JUMP_DUCK, JUMP_AIMUP, CLIMB, DEAD;

		boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		boolean isGround() { return this.equalsAny(STAND, WALK, DUCK, AIMUP); }
		boolean isDuck() { return this.equalsAny(DUCK, PRE_JUMP_DUCK, JUMP_DUCK); }
		boolean isPreJump() { return this.equalsAny(PRE_JUMP, PRE_JUMP_DUCK, PRE_JUMP_AIMUP); }
		boolean isJump() { return this.equalsAny(PRE_JUMP, PRE_JUMP_DUCK, PRE_JUMP_AIMUP, JUMP,
				JUMP_AIMUP, JUMP_DUCK); }
		boolean isAimUp() { return this.equalsAny(AIMUP, PRE_JUMP_AIMUP, JUMP_AIMUP); }
	}

	private Pit parent;
	private AgentHooks parentHooks;
	private PitSpine spine;
	private PlayerAgentSupervisor supervisor;
	private MoveState moveState;
	private float moveStateTimer;
	private boolean isFacingRight;
	private boolean isNextJumpAllowed;
	private float jumpForceTimer;
	private boolean isNextShotAllowed;
	private float shootCooldownTimer;
	private boolean takeDamageThisFrame;
	private float noDamageCooldown;
	private boolean isOnGroundHeadInTile;
	// list of powerups received during contact update
	private LinkedList<Powerup> powerupsReceived;
	private int heartsCollected;
	private int health;
	private RoomBox lastKnownRoom;

	PitBrain(Pit parent, AgentHooks parentHooks, PitSpine spine, boolean isFacingRight,
			Integer health, Integer heartsCollected) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.spine = spine;
		this.isFacingRight = isFacingRight;
		this.health = health != null ? health : START_HEALTH;
		this.heartsCollected = heartsCollected != null ? heartsCollected :  0;
		supervisor = new PlayerAgentSupervisor(parent, parentHooks);
		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		isNextJumpAllowed = false;	// false until land on solid ground
		jumpForceTimer = 0f;
		isNextShotAllowed = true;
		shootCooldownTimer = 0f;
		isOnGroundHeadInTile = false;
		powerupsReceived = new LinkedList<Powerup>();
		noDamageCooldown = 0f;
		takeDamageThisFrame = false;
		lastKnownRoom = null;
	}

	/*
	 * Check for and do head bumps during contact update, so bump tiles can show results of bump immediately
	 * by way of regular update.
	 */
	void processContactFrame() {
		// update last known room if not dead, so dead player moving through other RoomBoxes won't cause problems
		if(moveState != MoveState.DEAD && spine.getCurrentRoom() != null)
			lastKnownRoom = spine.getCurrentRoom();
	}

	SpriteFrameInput processFrame(FrameTime frameTime) {
		// if a script is running with no move advice then apply scripted body state and exit
		if(supervisor.isRunningScriptNoMoveAdvice()) {
			ScriptedAgentState scriptedState = supervisor.getScriptAgentState();
			spine.useScriptedBodyState(scriptedState.scriptedBodyState);
			isFacingRight = scriptedState.scriptedSpriteState.isFacingRight;
			// return null if scripted sprite is not visible
			if(!supervisor.getScriptAgentState().scriptedSpriteState.visible)
				return null;
			return getScriptedSpriteFrameInput(supervisor.getScriptAgentState().scriptedSpriteState, frameTime);
		}

		MoveAdvice4x2 moveAdvice = supervisor.pollMoveAdvice();

		processPowerupsReceived();
		processDamageTaken(frameTime);

		MoveState nextMoveState = getNextMoveState(moveAdvice);
		boolean moveStateChanged = nextMoveState != moveState;
		if(nextMoveState == MoveState.DEAD)
			processDeadMove(moveStateChanged);
		else {
			spine.checkDoBodySizeChange(nextMoveState.isDuck());

			if(nextMoveState.isGround())
				processGroundMove(moveAdvice, nextMoveState);
			else
				processAirMove(frameTime, moveAdvice, nextMoveState);

			// check/do facing direction change
			if(spine.isWalkingRight())
				isFacingRight = true;
			else if(spine.isWalkingLeft())
				isFacingRight = false;

			processShoot(frameTime, moveAdvice);

			// do space wrap last so that contacts are maintained
//			spine.checkDoSpaceWrap(lastKnownRoom);
		}

		if((moveState.isPreJump() && nextMoveState.isPreJump()) || (moveState.isJump() && nextMoveState.isJump()))
			moveStateTimer += frameTime.timeDelta;
		else
			moveStateTimer = moveState != nextMoveState ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;

		return new PitSpriteFrameInput(spine.getPosition(), isFacingRight, frameTime, moveState,
				(noDamageCooldown > 0f), (shootCooldownTimer > 0f), isOnGroundHeadInTile,
				spine.isMovingInDir(Direction4.UP), Direction4.NONE);
	}

	private SpriteFrameInput getScriptedSpriteFrameInput(ScriptedSpriteState sss, FrameTime frameTime) {
		MoveState scriptedMoveState;
		switch(sss.spriteState) {
			case MOVE:
				scriptedMoveState = MoveState.WALK;
				break;
			case CLIMB:
				scriptedMoveState = MoveState.CLIMB;
				break;
			case STAND:
			default:
				scriptedMoveState = MoveState.STAND;
				break;
		}
		return new PitSpriteFrameInput(sss.position, sss.isFacingRight, frameTime, scriptedMoveState, false, false,
				false, false, sss.moveDir);
	}

	private void addHealth(int healAmt) {
		// add health and cap it to MAX_HEALTH
		health = health+healAmt > MAX_HEALTH ? MAX_HEALTH : health+healAmt;
	}

	private void processDamageTaken(FrameTime frameTime) {
		// check for contact with scroll kill box (insta-kill)
		if(spine.isContactScrollKillBox()) {
			health = 0;
			return;
		}
		// if invulnerable to damage then exit
		else if(noDamageCooldown > 0f) {
			noDamageCooldown -= frameTime.timeDelta;
			takeDamageThisFrame = false;
			return;
		}
		// if no damage taken this frame then exit
		else if(!takeDamageThisFrame)
			return;

		// apply damage against health, health cannot be less than zero
		health = health-1 > 0 ? health-1 : 0;
		// reset frame take damage amount
		takeDamageThisFrame = false;
		// start no damage period
		noDamageCooldown = NO_DAMAGE_TIME;
		// ouchie sound
		parentHooks.getEar().playSound(KidIcarusAudio.Sound.Pit.HURT);
	}

	private void processPowerupsReceived() {
		for(Powerup pu : powerupsReceived) {
			if(pu instanceof KidIcarusPow.AngelHeartPow) {
				heartsCollected += ((KidIcarusPow.AngelHeartPow) pu).getNumHearts();
				if(heartsCollected > MAX_HEARTS_COLLECTED)
					heartsCollected = MAX_HEARTS_COLLECTED;
			}
			else if(pu instanceof KidIcarusPow.ChaliceHealthPow)
				addHealth(((KidIcarusPow.ChaliceHealthPow) pu).getHealAmount());
			else if(pu.getPowerupCharacter() != PowChar.PIT)
				supervisor.receiveNonCharPowerup(pu);
		}
		powerupsReceived.clear();
	}

	private MoveState getNextMoveState(MoveAdvice4x2 moveAdvice) {
		if(moveState == MoveState.DEAD || spine.isContactDespawn() || health <= 0)
			return MoveState.DEAD;
		// if [on ground flag is true] and agent isn't [moving upward while in air move state], then do ground move
		else if(spine.isOnGround() && !(spine.isMovingInDir(Direction4.UP) && !moveState.isGround()))
			return getNextMoveStateGround(moveAdvice);
		// do air move
		else
			return getNextMoveStateAir(moveAdvice);
	}

	private MoveState getNextMoveStateGround(MoveAdvice4x2 moveAdvice) {
		// if advised jump, and allowed to jump, and not aiming up, and not currently ducking...
		if(moveAdvice.action1 && isNextJumpAllowed && moveState != MoveState.AIMUP && moveState != MoveState.DUCK) {
			// Pit can start to jump and do one other thing:
			//     1) Duck, or
			//     2) Aim Up
			// Duck takes priority over Aim Up.
			if(moveAdvice.moveDown)
				return MoveState.PRE_JUMP_DUCK;
			else if(moveAdvice.moveUp)
				return MoveState.PRE_JUMP_AIMUP;
			else
				return MoveState.PRE_JUMP;
		}
		else if(moveAdvice.moveDown)
			return MoveState.DUCK;
		else if(moveAdvice.moveUp)
			return MoveState.AIMUP;
		// if advised move horizontally, or if already moving horizontally, then return RUN
		else if(moveAdvice.moveRight || moveAdvice.moveLeft || !spine.isStandingStill())
			return MoveState.WALK;
		else
			return MoveState.STAND;
	}

	private MoveState getNextMoveStateAir(MoveAdvice4x2 moveAdvice) {
		if(moveState.isPreJump() && moveStateTimer <= PRE_JUMP_TIME) {
			if(moveAdvice.moveDown)
				return MoveState.PRE_JUMP_DUCK;
			else if(moveAdvice.moveUp)
				return MoveState.PRE_JUMP_AIMUP;
			else
				return MoveState.PRE_JUMP;
		}
		else {
			if(moveAdvice.moveDown)
				return MoveState.JUMP_DUCK;
			else if(moveAdvice.moveUp)
				return MoveState.JUMP_AIMUP;
			else
				return MoveState.JUMP;
		}
	}

	private void processDeadMove(boolean moveStateChanged) {
		// if newly dead then disable contacts and start dead sound
		if(moveStateChanged) {
			spine.applyDead();
			parentHooks.getEar().stopAllMusic();
			parentHooks.getEar().startSinglePlayMusic(KidIcarusAudio.Music.PIT_DIE);
		}
		// ... and if died a long time ago then do game over
		else if(moveStateTimer > DEAD_DELAY_TIME)
				supervisor.setGameOver();
		// ... else do nothing.
	}

	private void processGroundMove(MoveAdvice4x2 moveAdvice, MoveState nextMoveState) {
		// next jump changes to allowed when on ground and not advising jump move
		if(!moveAdvice.action1)
			isNextJumpAllowed = true;

		// update the "head-in-tile" flag, will be used by sprite for correct sprite offset
		isOnGroundHeadInTile = spine.isHeadInTile();

		Direction4 moveDir = Direction4.NONE;
		switch(nextMoveState) {
			case STAND:
			case WALK:
			case DUCK:
				// check for move application
				if(moveAdvice.moveRight)
					moveDir = Direction4.RIGHT;
				else if(moveAdvice.moveLeft)
					moveDir = Direction4.LEFT;

				// if head is in tile and body is not moving, then facing direction can change
				if(isOnGroundHeadInTile && spine.isStandingStill()) {
					if(moveAdvice.moveRight)
						isFacingRight = true;
					else if(moveAdvice.moveLeft)
						isFacingRight = false;
				}

				break;
			case AIMUP:
				// Aimup freezes x velocity while on ground - by not zeroing Y velocity, this code should
				// avoid problems with vertical moving platforms - still problems with horizontally moving
				// platforms though - unless zeroVelocity checks for relative velocity and uses relative zero?
				// TODO !
				spine.zeroVelocity(true, false);
				if(moveAdvice.moveRight)
					isFacingRight = true;
				else if(moveAdvice.moveLeft)
					isFacingRight = false;
				break;
			default:
				throw new IllegalStateException("Wrong ground nextMoveState = " + nextMoveState);
		}

		// if not ducking and head is stuck in tile then disable horizontal move
		boolean disableHMove = !nextMoveState.isDuck() && isOnGroundHeadInTile;
		// move right advice takes priority over move left advice, but disable move is highest priority
		if(!disableHMove && moveDir == Direction4.RIGHT)
			spine.applyWalkMove(true);
		else if(!disableHMove && moveDir == Direction4.LEFT)
			spine.applyWalkMove(false);
		else
			spine.applyStopMove();
	}

	private void processAirMove(FrameTime frameTime, MoveAdvice4x2 moveAdvice, MoveState nextMoveState) {
		isOnGroundHeadInTile = false;
		switch(nextMoveState) {
			case PRE_JUMP:
			case PRE_JUMP_DUCK:
			case PRE_JUMP_AIMUP:
				// if previously on ground and advised jump, then jump
				if(moveState.isGround() && moveAdvice.action1) {
					isNextJumpAllowed = false;
					jumpForceTimer = PRE_JUMP_TIME+JUMPUP_FORCE_TIME;
					spine.applyJumpVelocity();
					parentHooks.getEar().playSound(KidIcarusAudio.Sound.Pit.JUMP);
				}
				else if(moveStateTimer <= PRE_JUMP_TIME)
					spine.applyJumpVelocity();
				break;
			case JUMP:
			case JUMP_DUCK:
			case JUMP_AIMUP:
				// This takes care of player falling off ledge - player cannot jump again until at least
				// one frame has elapsed where player was on ground and not advised to  jump (to prevent
				// re-jump bouncing).
				isNextJumpAllowed = false;

				// if jump force continues and jump is advised then do jump force
				if(jumpForceTimer > 0f && moveAdvice.action1)
					spine.applyJumpForce(jumpForceTimer-PRE_JUMP_TIME, JUMPUP_FORCE_TIME);
				break;
			default:
				throw new IllegalStateException("Wrong air nextMoveState = " + nextMoveState);
		}

		// disallow jump force until next jump if [jump advice stops] or [body stops moving up]
		if(!moveAdvice.action1 || !spine.isMovingInDir(Direction4.UP))
			jumpForceTimer = 0f;

		// move right advice takes priority over move left advice
		if(moveAdvice.moveRight)
			spine.applyAirMove(true);
		else if(moveAdvice.moveLeft)
			spine.applyAirMove(false);

		// decrement jump force timer
		jumpForceTimer = jumpForceTimer > frameTime.timeDelta ? jumpForceTimer-frameTime.timeDelta : 0f;
	}

	private void processShoot(FrameTime frameTime, MoveAdvice4x2 moveAdvice) {
		if(moveAdvice.action0 && isNextShotAllowed && shootCooldownTimer <= 0f && !moveState.isDuck())
			doShoot();
		else if(!moveAdvice.action0)
			isNextShotAllowed = true;

		// GLITCH when Pit ducks, the shoot cooldown timer resets and Pit can shoot again immediately
		if(moveState.isDuck())
			shootCooldownTimer = 0f;
		else {
			shootCooldownTimer =
					shootCooldownTimer > frameTime.timeDelta ? shootCooldownTimer-frameTime.timeDelta : 0f;
		}
	}

	private void doShoot() {
		isNextShotAllowed = false;
		shootCooldownTimer = SHOOT_COOLDOWN;

		// calculate position and velocity of shot based on samus' orientation
		Direction4 arrowDir;
		Vector2 velocity = new Vector2();
		Vector2 position = new Vector2();
		if(moveState.isAimUp()) {
			arrowDir = Direction4.UP;
			velocity.set(0f, SHOT_VEL);
			if(isFacingRight)
				position.set(SHOT_OFFSET_UP).add(spine.getPosition());
			else
				position.set(SHOT_OFFSET_UP).scl(-1, 1).add(spine.getPosition());
		}
		else {
			if(isFacingRight) {
				arrowDir = Direction4.RIGHT;
				velocity.set(SHOT_VEL, 0f);
				position.set(SHOT_OFFSET_RIGHT).add(spine.getPosition());
			}
			else {
				arrowDir = Direction4.LEFT;
				velocity.set(-SHOT_VEL, 0f);
				position.set(SHOT_OFFSET_RIGHT).scl(-1, 1).add(spine.getPosition());
			}

			if(isOnGroundHeadInTile)
				position.add(SHOT_OFFSET_HEAD_IN_TILE);
		}

		// create shot; if the spawn point of shot is in a solid tile then the shot must show for a short time
		parentHooks.createAgent(PitArrow.makeAP(parent, position, velocity, arrowDir,
				spine.isMapPointSolid(position)));
		parentHooks.getEar().playSound(KidIcarusAudio.Sound.Pit.SHOOT);
	}

	Vector2 getPosition() {
		// use the "real" position of Pit, which is independent of body size, to improve smooth screen scroll
		Vector2 offset = new Vector2(0f, 0f);
		if(isOnGroundHeadInTile || moveState.isDuck())
			offset.set(DUCK_POS_OFFSET);
		return spine.getPosition().cpy().add(offset);
	}

	RoomBox getCurrentRoom() {
		return lastKnownRoom;
	}

	boolean onTakeDamage() {
		// don't take more damage if damage taken already this frame, or if invulnerable
		if(moveState == MoveState.DEAD || takeDamageThisFrame || noDamageCooldown > 0f)
			return false;
		takeDamageThisFrame = true;
		return true;
	}

	boolean onTakePowerup(Powerup pu) {
		if(moveState == MoveState.DEAD)
			return false;
		powerupsReceived.add(pu);
		return true;
	}

	PlayerAgentSupervisor getSupervisor() {
		return supervisor;
	}

	boolean isFacingRight() {
		return isFacingRight;
	}

	Integer getHealth() {
		return health;
	}

	Integer getHeartsCollected() {
		return heartsCollected;
	}
}
