package kidridicarus.agency;

public class AgentRemovalListener { 
	public interface AgentRemovalCallback { public void preAgentRemoval(); public void postAgentRemoval(); }

	Agent myAgent;
	Agent otherAgent;
	public AgentRemovalCallback callback;

	public AgentRemovalListener(Agent myAgent, Agent otherAgent, AgentRemovalCallback callback) {
		this.myAgent = myAgent;
		this.otherAgent = otherAgent;
		this.callback = callback;
	}
}
