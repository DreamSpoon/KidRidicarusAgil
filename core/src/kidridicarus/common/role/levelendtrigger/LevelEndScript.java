package kidridicarus.common.role.levelendtrigger;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.RoleScript;
import kidridicarus.story.rolescript.ScriptedRoleState;

class LevelEndScript implements RoleScript {
	private static final float LEVELEND_WAIT = 4f;

	private LevelEndTrigger parent;
	private RoleScriptHooks scriptHooks;
	private ScriptedRoleState curScriptRoleState;
	private float stateTimer;
	private String nextLevelName;

	LevelEndScript(LevelEndTrigger parent, String nextLevelName) {
		this.parent = parent;
		this.nextLevelName = nextLevelName;
		scriptHooks = null;
		curScriptRoleState = null;
		stateTimer = 0f;
	}

	@Override
	public void startScript(RoleHooks roleHooks, RoleScriptHooks scriptHooks,
			ScriptedRoleState beginScriptRoleState) {
		this.scriptHooks = scriptHooks;
		this.curScriptRoleState = beginScriptRoleState.cpy();

		// disable character contacts and hide the sprite
		curScriptRoleState.scriptedBodyState.contactEnabled = false;
		curScriptRoleState.scriptedBodyState.gravityFactor = 0f;
		curScriptRoleState.scriptedSpriteState.visible = false;

		// hoist the end of level flag
		parent.onTakeTrigger();
	}

	@Override
	public boolean update(FrameTime frameTime) {
		if(stateTimer > LEVELEND_WAIT) {
			scriptHooks.gotoNextLevel(nextLevelName);
			// end script updates
			return false;
		}
		stateTimer += frameTime.timeDelta;
		return true;
	}

	@Override
	public ScriptedRoleState getScriptRoleState() {
		return curScriptRoleState;
	}

	@Override
	public boolean isOverridable(RoleScript nextScript) {
		return false;
	}
}
