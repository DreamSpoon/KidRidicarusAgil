package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playerspawner.PlayerSpawner;
import kidridicarus.common.tool.AP_Tool;

class KidIcarusDoorBrain {
	private KidIcarusDoor parent;
	private AgentHooks parentHooks;
	private KidIcarusDoorSpine spine;
	private boolean isOpened;
	private String exitSpawnerName;
	private boolean isTriggered;

	KidIcarusDoorBrain(KidIcarusDoor parent, AgentHooks parentHooks, KidIcarusDoorSpine spine, boolean isOpened,
			String exitSpawnerName) {
		this.parent = parent;
		this.parentHooks = parentHooks;
		this.spine = spine;
		this.isOpened = isOpened;
		this.exitSpawnerName = exitSpawnerName;
		isTriggered = false;
	}

	void processContactFrame() {
		// exit if not opened, or if zero players contacting door
		if(!isOpened || spine.getPlayerContacts().isEmpty())
			return;
		// exit if spawner doesn't exist or is the wrong class
		Agent exitSpawner = AP_Tool.getNamedAgent(exitSpawnerName, parentHooks);
		if(!(exitSpawner instanceof PlayerSpawner)) {
			throw new IllegalArgumentException("Kid Icarus Door exit spawner is not instance of "+
					PlayerSpawner.class.getName()+", exitSpawnerName="+exitSpawnerName+
					", exitSpawner="+exitSpawner);
		}
		// pass a separate door script to each player contacting this door
		for(PlayerAgent agent : spine.getPlayerContacts())
			agent.getSupervisor().startScript(new KidIcarusDoorScript(parent, exitSpawner));
	}

	KidIcarusDoorSpriteFrameInput processFrame() {
		if(isTriggered) {
			isTriggered = false;
			isOpened = !isOpened;
			spine.setOpened(isOpened);
		}
		return new KidIcarusDoorSpriteFrameInput(spine.getPosition(), isOpened);
	}

	void onTakeTrigger() {
		isTriggered = true;
	}
}
