package kidridicarus.story.rolescript;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.story.RoleHooks;

public interface RoleScript {
	/*
	 * A superclass of RoleSupervisor will create and implement the hooks, to allow a running script to give
	 * information to, and/or trigger actions by, the RoleSupervisor. Scripts must be able to do more than
	 * move a sprite/body around on the screen.
	 */
	public interface RoleScriptHooks {
		public void gotoNextLevel(String levelName);
		// also include start music, stop music, etc.
	}

	public void startScript(RoleHooks roleHooks, RoleScriptHooks scriptHooks,
			ScriptedRoleState beginScriptAgentState);
	public boolean update(FrameTime frameTime);	// return true to continue running script, return false to stop
	public ScriptedRoleState getScriptRoleState();
	// The next script (the script which is requesting the override) is passed so current script can
	// check type of next script (or even call methods of the next script!), to verify/prioritize overrides.
	public boolean isOverridable(RoleScript nextScript);
}
