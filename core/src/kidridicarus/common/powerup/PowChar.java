package kidridicarus.common.powerup;

import kidridicarus.game.KidIcarus.KidIcarusKV;

public enum PowChar {
	NONE(""),
	PIT(KidIcarusKV.AgentClassAlias.VAL_PIT);

	private String agentClassAlias;

	private PowChar(String agentClassAlias) {
		this.agentClassAlias = agentClassAlias;
	}

	public String getAgentClassAlias() {
		return agentClassAlias;
	}
}
