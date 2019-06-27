package kidridicarus.agency;

public class AgentContactHalf {
	public Agent agent;
	public AgentBody agentBody;
	public AgentFixture agentFixture;

	public AgentContactHalf(Agent agent, AgentBody agentBody, AgentFixture agentFixture) {
		this.agent = agent;
		this.agentBody = agentBody;
		this.agentFixture = agentFixture;
	}
}
