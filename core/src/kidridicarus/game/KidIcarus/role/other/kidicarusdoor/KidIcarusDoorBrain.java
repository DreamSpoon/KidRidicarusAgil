package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.role.playerspawner.PlayerSpawner;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

class KidIcarusDoorBrain {
	private KidIcarusDoor parent;
	private RoleHooks parentRoleHooks;
	private KidIcarusDoorSpine spine;
	private boolean isOpened;
	private String exitSpawnerName;
	private boolean isTriggered;

	KidIcarusDoorBrain(KidIcarusDoor parent, RoleHooks parentRoleHooks, KidIcarusDoorSpine spine, boolean isOpened,
			String exitSpawnerName) {
		this.parent = parent;
		this.parentRoleHooks = parentRoleHooks;
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
		Role exitSpawner = RP_Tool.getNamedRole(exitSpawnerName, parentRoleHooks);
		if(!(exitSpawner instanceof PlayerSpawner)) {
			throw new IllegalArgumentException("Kid Icarus Door exit spawner is not instance of "+
					PlayerSpawner.class.getName()+", exitSpawnerName="+exitSpawnerName+
					", exitSpawner="+exitSpawner);
		}
		// pass a unique door script to each player contacting this door
		for(PlayerRole role : spine.getPlayerContacts())
			role.getSupervisor().startScript(new KidIcarusDoorScript(parent, exitSpawner));
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
