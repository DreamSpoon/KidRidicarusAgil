package kidridicarus.story.rolescript;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.RoleScript.RoleScriptHooks;

/*
 * *Basic* order of operations for game loop and scripts:
 *   1) Get user input and call script update methods
 *   2) Run game world update (scripts may be started during game world update)
 *   4) Draw game world
 * 
 * Scripts may be started during game world update, but these scripts do not receive their first update until
 * next frame update. However, it is possible for the scripts to modify the player Role in the same frame in which
 * they were started, if the player Role checks for script starts during it's update frame.
 * The 'continue running' flag is needed so the player Role can check if the script was running at any point during
 * the current update frame. If the 'continue running' flag isn't used then the script's final Role update state
 * might be ignored by the player Role.
 */
public class RoleScriptRunner {
	private RoleScript currentScript;
	private boolean isRunning;
	private boolean continueRunning;
	private RoleHooks roleSupervisorHooks;

	public RoleScriptRunner(RoleHooks roleSupervisorHooks) {
		this.roleSupervisorHooks = roleSupervisorHooks;
		currentScript = null;
		isRunning = false;
		continueRunning = false;
	}

	/*
	 * Returns true if script was started, otherwise returns false.
	 * Takes the beginning state of the Role.
	 */
	public boolean startScript(RoleScript roleScript, RoleScriptHooks scriptHooks,
			ScriptedRoleState startRoleState) {
		// if a script is already running and cannot be overridden then return false
		if(isRunning && !currentScript.isOverridable(roleScript))
			return false;
		// start the script
		isRunning = true;
		continueRunning = true;
		currentScript = roleScript;
		currentScript.startScript(roleSupervisorHooks, scriptHooks, startRoleState);
		return true;
	}

	public void preUpdateAgency(FrameTime frameTime) {
		if(!isRunning)
			return;
		continueRunning = currentScript.update(frameTime);
	}

	public void postUpdateAgency() {
		if(!isRunning)
			return;
		isRunning = continueRunning;
	}

	public ScriptedRoleState getScriptRoleState() {
		return currentScript.getScriptRoleState();
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isRunningMoveAdvice() {
		if(!isRunning)
			return false;
		ScriptedRoleState srs = currentScript.getScriptRoleState();
		if(srs == null)
			return false;
		return srs.scriptedMoveAdvice != null;
	}
}
