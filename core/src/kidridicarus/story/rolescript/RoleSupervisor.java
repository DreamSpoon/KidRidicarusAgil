package kidridicarus.story.rolescript;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.tool.MoveAdvice4x2;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.RoleScript.RoleScriptHooks;

/*
 * Supervisor is expected to handle stuff for Roles:
 *   -scripted actions
 *   -relaying advice to the Role
 *   -...
 */
public abstract class RoleSupervisor {
	protected final Role supervisedRole;
	protected final RoleHooks supervisedRoleHooks;
	private final RoleScriptRunner scriptRunner;

	public abstract void setMoveAdvice(MoveAdvice4x2 moveAdvice);
	// internalPollMoveAdvice method to be implemented by superclass, for use by this class only.
	// Other classes that poll move advice from a superclass must call pollMoveAdvice method.
	protected abstract MoveAdvice4x2 internalPollMoveAdvice();
	public abstract boolean isAtLevelEnd();
	public abstract String getNextLevelFilename();
	public abstract boolean isGameOver();

	/*
	 * Convert the Role state information into a simpler script Role state format, and return it.
	 * Script Role state is used to initialize/direct the Role state when a script is started/running.
	 */
	protected abstract ScriptedRoleState getCurrentScriptRoleState();

	protected abstract RoleScriptHooks getRoleScriptHooks();

	public RoleSupervisor(Role supervisedRole, RoleHooks supervisedRoleHooks) {
		scriptRunner = new RoleScriptRunner(supervisedRoleHooks);
		this.supervisedRole = supervisedRole;
		this.supervisedRoleHooks = supervisedRoleHooks;
	}

	public void preUpdateAgency(FrameTime frameTime) {
		scriptRunner.preUpdateAgency(frameTime);
	}

	/*
	 * Postupdate the scriptrunner, and check for room changes; which may lead to view changes, music changes, etc.
	 */
	public void postUpdateAgency() {
		scriptRunner.postUpdateAgency();
	}

	// return false if already running a script, otherwise start using the given script and return true 
	public boolean startScript(RoleScript roleScript) {
		return scriptRunner.startScript(roleScript, getRoleScriptHooks(), getCurrentScriptRoleState());
	}

	public ScriptedRoleState getScriptRoleState() {
		return scriptRunner.getScriptRoleState();
	}

	public boolean isRunningScript() {
		return scriptRunner.isRunning();
	}

	public boolean isRunningScriptMoveAdvice() {
		return scriptRunner.isRunningMoveAdvice();
	}

	public boolean isRunningScriptNoMoveAdvice() {
		return scriptRunner.isRunning() && !scriptRunner.isRunningMoveAdvice();
	}

	/*
	 * Returns scripted move advice if scripted move advice script is running.
	 * Otherwise returns regular user move advice.
	 */
	public MoveAdvice4x2 pollMoveAdvice() {
		if(scriptRunner.isRunningMoveAdvice())
			return scriptRunner.getScriptRoleState().scriptedMoveAdvice;
		else
			return internalPollMoveAdvice();
	}
}
