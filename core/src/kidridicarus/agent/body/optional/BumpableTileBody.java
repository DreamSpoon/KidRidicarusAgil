package kidridicarus.agent.body.optional;

import kidridicarus.agent.Agent;

public interface BumpableTileBody {
	// mario jump punching the block from below
	public void onBumpTile(Agent bumpingAgent);
}