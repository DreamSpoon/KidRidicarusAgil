package kidridicarus.agency.agent.body.optional;

import kidridicarus.agency.agent.Agent;

public interface BumpableTileBody {
	// mario jump punching the block from below
	public void onBumpTile(Agent bumpingAgent);
}