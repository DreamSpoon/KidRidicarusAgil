package kidridicarus.game.KidIcarus.role.other.kidicarusdoor.script;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.common.role.playerspawner.PlayerSpawner;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.role.other.kidicarusdoor.KidIcarusDoor;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.ScriptedRoleState;
import kidridicarus.story.tool.RP_Tool;

/*
 * Name: Kid Icarus Door Script
 * Desc: Transport player from one place to another, and close the door behind them.
 */
public class KidIcarusDoorScript extends Role {
	private static final float WAIT_TIME_ENTER = 1f;
	private static final float WAIT_TIME_EXIT = 0.75f;

	private enum ScriptState { ENTER, EXIT, END1 }

	private KidIcarusDoor parentDoorRole;
	private ScriptableRole childRole;
	private ScriptedRoleState childRoleState;
	private Vector2 exitPos;
	private Direction4 exitDir;
	private ScriptState moveState;
	private float startTime;

	public KidIcarusDoorScript(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.childRole = (ScriptableRole) RP_Tool.getChildRole(properties);
		// save ref to door so door can be "closed" after player enters door
		this.parentDoorRole = (KidIcarusDoor) RP_Tool.getParentRole(properties);
		// save exit position for player exit
		Role exitSpawner = properties.get(CommonKV.KEY_EXIT_SPAWNER, null, Role.class);
		this.exitPos = RP_Tool.getCenter(exitSpawner);
		if(this.exitPos == null) {
			throw new IllegalArgumentException("Cannot create Kid Icarus door script from exit spawner " +
					"with no defined position, spawner=" + exitSpawner);
		}
		this.exitDir = exitSpawner.getAgent().getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
		this.moveState = ScriptState.ENTER;
		this.childRole.scriptSetEnabled(true);
		this.childRoleState = getInitChildRoleScriptState();
		this.childRole.scriptSetFrameState(this.childRoleState);
		this.startTime = myAgentHooks.getAbsTime();
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.SCRIPT_UPDATE, new AgentUpdateListener() {
					@Override
					public void update(FrameTime frameTime) { updateScript(); }
				});
	}

	private ScriptedRoleState getInitChildRoleScriptState() {
		// copy the current role state, maintaining current sprite state and position
		ScriptedRoleState initState = childRole.scriptGetFrameState().cpy();
		initState.isOverrideAllowed = false;
		// maintain body position, but with no movement and no contact
		initState.scriptedBodyState.contactEnabled = false;
		initState.scriptedBodyState.gravityFactor = 0f;
		initState.scriptedBodyState.velocity.set(0f, 0f);
		return initState;
	}

	private void updateScript() {
		ScriptState nextMoveState = getNextScriptState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case ENTER:
				break;
			case EXIT:
				if(isMoveStateChange) {
					// close the door
					parentDoorRole.onTakeTrigger();
					// set body position and sprite position to to exit position
					childRoleState.scriptedBodyState.position.set(exitPos);
					childRoleState.scriptedSpriteState.position.set(exitPos);

					// set player facing direction if the exit spawner has direction property
					if(exitDir == Direction4.RIGHT)
						childRoleState.scriptedSpriteState.isFacingRight = true;
					else if(exitDir == Direction4.LEFT)
						childRoleState.scriptedSpriteState.isFacingRight = false;
				}
				break;
			case END1:
				childRole.scriptSetEnabled(false);
				myAgentHooks.removeThisAgent();
				return;
		}
		moveState = nextMoveState;
		childRole.scriptSetFrameState(childRoleState);
	}

	private ScriptState getNextScriptState() {
		if(moveState == ScriptState.ENTER) {
			if(myAgentHooks.getAbsTime() - startTime > WAIT_TIME_ENTER)
				return ScriptState.EXIT;
			else
				return ScriptState.ENTER;
		}
		else if(moveState == ScriptState.EXIT) {
			if(myAgentHooks.getAbsTime() - startTime > WAIT_TIME_ENTER+WAIT_TIME_EXIT)
				return ScriptState.END1;
			else
				return ScriptState.EXIT;
		}
		else
			return ScriptState.END1;
	}

	public static ObjectProperties makeRP(ScriptableRole childRole, KidIcarusDoor parentDoorRole,
			PlayerSpawner exitSpawner) {
		if(!(childRole instanceof PlayerRole)) {
			throw new IllegalStateException(
					"Cannot create Kid Icarus door script with childRole that is not instance of a PlayerRole.");
		}
		ObjectProperties rp = RP_Tool.createRP(KidIcarusKV.RoleClassAlias.VAL_DOOR_SCRIPT);
		rp.put(CommonKV.KEY_CHILD_ROLE, childRole);
		rp.put(CommonKV.KEY_PARENT_ROLE, parentDoorRole);
		rp.put(CommonKV.KEY_EXIT_SPAWNER, exitSpawner);
		return rp;
	}
}
