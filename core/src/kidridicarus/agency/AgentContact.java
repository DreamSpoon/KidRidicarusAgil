package kidridicarus.agency;

public class AgentContact {
	public Agent agentA;
	public Agent agentB;
	public AgentBody agentBodyA;
	public AgentBody agentBodyB;
	public AgentFixture agentFixtureA;
	public AgentFixture agentFixtureB;

	public AgentContact(Agent agentA, Agent agentB, AgentBody agentBodyA, AgentBody agentBodyB,
			AgentFixture agentFixtureA, AgentFixture agentFixtureB) {
		this.agentA = agentA;
		this.agentB = agentB;
		this.agentBodyA = agentBodyA;
		this.agentBodyB = agentBodyB;
		this.agentFixtureA = agentFixtureA;
		this.agentFixtureB = agentFixtureB;
	}

	// get the other agent/body/fixture that contact that contacted the given AgentFixture
	public AgentContactHalf getOtherHalf(AgentFixture agentFixture) {
		// if the given fixture is from Agent A, then return info about Agent B
		if(agentFixture == this.agentFixtureA)
			return new AgentContactHalf(this.agentB, this.agentBodyB, this.agentFixtureB);
		// else return info about Agent A
		else
			return new AgentContactHalf(this.agentA, this.agentBodyA, this.agentFixtureA);
	}

	public String toString() {
		return "AgentContact={ agentA="+agentA+", agentB="+agentB+" };";
	}
}
