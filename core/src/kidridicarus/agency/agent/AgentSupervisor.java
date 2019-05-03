package kidridicarus.agency.agent;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.AgentScriptRunner;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.common.tool.MoveAdvice4x4;

/*
 * Supervisor is expected to handle stuff for Agents:
 *   -scripted actions
 *   -relaying advice to the agent
 *   -...
 */
public abstract class AgentSupervisor {
	protected Agent supervisedAgent;
	private AgentScriptRunner scriptRunner;

	public abstract void setMoveAdvice(MoveAdvice4x4 moveAdvice);
	// internalPollMoveAdvice method to be implemented by superclass, for use by this class only.
	// Other classes that poll move advice from a superclass must call pollMoveAdvice method.
	protected abstract MoveAdvice4x4 internalPollMoveAdvice();
	public abstract boolean isAtLevelEnd();
	public abstract String getNextLevelFilename();
	public abstract boolean isGameOver();

	/*
	 * Convert the agent state information into a simpler script agent state format, and return it.
	 * Script agent state is used to initialize/direct the agent state when a script is started/running.
	 */
	protected abstract ScriptedAgentState getCurrentScriptAgentState();

	protected abstract AgentScriptHooks getAgentScriptHooks();

	public AgentSupervisor(Agent supervisedAgent) {
		scriptRunner = new AgentScriptRunner(this);
		this.supervisedAgent = supervisedAgent;
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
	public boolean startScript(AgentScript agentScript) {
		return scriptRunner.startScript(agentScript, getAgentScriptHooks(), getCurrentScriptAgentState());
	}

	public ScriptedAgentState getScriptAgentState() {
		return scriptRunner.getScriptAgentState();
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
	public MoveAdvice4x4 pollMoveAdvice() {
		if(scriptRunner.isRunningMoveAdvice())
			return scriptRunner.getScriptAgentState().scriptedMoveAdvice;
		else
			return internalPollMoveAdvice();
	}

	public Agency getAgency() {
		return supervisedAgent.getAgency();
	}
}
