package kidridicarus.agency;

public class AgentHooksBundle {
	public final Agent agent;
	public final AgentHooks agentHooks;
	public final PhysicsHooks physHooks;
	public final AudioHooks audioHooks;
	public final GfxHooks gfxHooks;

	public AgentHooksBundle(Agent agent, AgentHooks agentHooks, PhysicsHooks physHooks, AudioHooks audioHooks,
			GfxHooks gfxHooks) {
		this.agent = agent;
		this.agentHooks = agentHooks;
		this.physHooks = physHooks;
		this.audioHooks = audioHooks;
		this.gfxHooks = gfxHooks;
	}
}
