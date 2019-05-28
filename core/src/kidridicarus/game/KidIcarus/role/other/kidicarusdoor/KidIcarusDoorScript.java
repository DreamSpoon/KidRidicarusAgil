package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.RoleScript;
import kidridicarus.story.rolescript.ScriptedRoleState;
import kidridicarus.story.tool.RP_Tool;

/*
 * Name: Kid Icarus Door Script
 * Desc: Transport player from one place to another, and close the door behind them.
 */
class KidIcarusDoorScript implements RoleScript {
	private static final float WAIT_TIME_ENTER = 1f;
	private static final float WAIT_TIME_EXIT = 0.75f;

	private enum MoveState { ENTER, EXIT, END }

	private ScriptedRoleState currentScriptRoleState;
	private Vector2 exitPos;
	private float moveStateTimer;
	private MoveState moveState;
	private KidIcarusDoor entranceDoor;
	private Direction4 exitDir;

	KidIcarusDoorScript(KidIcarusDoor entranceDoor, Role exitSpawner) {
		moveStateTimer = 0f;
		moveState = MoveState.ENTER;
		// save ref to door so door can be "closed" after player enters door
		this.entranceDoor = entranceDoor;
		// save exit position for player exit
		exitPos = RP_Tool.getCenter(exitSpawner);
		if(exitPos == null) {
			throw new IllegalArgumentException("Cannot create Kid Icarus door script from exit spawner " +
					"with no defined position, spawner=" + exitSpawner);
		}
		exitDir = exitSpawner.getAgent().getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
	}

	@Override
	public void startScript(RoleHooks roleHooks, RoleScriptHooks scriptHooks,
			ScriptedRoleState beginScriptRoleState) {
		// error if this script is used more than once - i.e. script restart not allowed
		if(moveState != MoveState.ENTER)
			throw new IllegalStateException("Script restart not allowed.");
		// copy the current state, but disable contacts and movement
		currentScriptRoleState = beginScriptRoleState.cpy();
		currentScriptRoleState.scriptedBodyState.contactEnabled = false;
		currentScriptRoleState.scriptedBodyState.gravityFactor = 0f;
	}

	@Override
	public boolean update(FrameTime frameTime) {
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case ENTER:
				break;
			case EXIT:
				if(isMoveStateChange) {
					// close the door
					entranceDoor.onTakeTrigger();
					// set body and sprite position to to exit position
					currentScriptRoleState.scriptedBodyState.position.set(exitPos);
					currentScriptRoleState.scriptedSpriteState.position.set(exitPos);

					// set player facing direction if the exit spawner has direction property
					if(exitDir == Direction4.RIGHT)
						currentScriptRoleState.scriptedSpriteState.isFacingRight = true;
					else if(exitDir == Direction4.LEFT)
						currentScriptRoleState.scriptedSpriteState.isFacingRight = false;
				}
				break;
			case END:
				if(isMoveStateChange) {
					// re-enable contacts
					currentScriptRoleState.scriptedBodyState.contactEnabled = true;
					currentScriptRoleState.scriptedBodyState.gravityFactor = 1f;
				}
				return false;
		}

		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		return true;
	}

	private MoveState getNextMoveState() {
		if(moveState == MoveState.ENTER) {
			if(moveStateTimer > WAIT_TIME_ENTER)
				return MoveState.EXIT;
			else
				return MoveState.ENTER;
		}
		else if(moveState == MoveState.EXIT) {
			if(moveStateTimer > WAIT_TIME_EXIT)
				return MoveState.END;
			else
				return MoveState.EXIT;
		}
		else
			return MoveState.END;
	}

	@Override
	public ScriptedRoleState getScriptRoleState() {
		return this.currentScriptRoleState;
	}

	@Override
	public boolean isOverridable(RoleScript nextScript) {
		return false;
	}
}
