package kidridicarus.agency;

public class AgentRemovalListener { 
	public interface AgentRemovalCallback { public void preAgentRemoval(); public void postAgentRemoval(); }

	public AgentRemovalCallback callback;
	Agent myAgent;
	Agent otherAgent;

	public AgentRemovalListener(Agent myAgent, Agent otherAgent, AgentRemovalCallback callback) {
		this.myAgent = myAgent;
		this.otherAgent = otherAgent;
		this.callback = callback;
	}
}
