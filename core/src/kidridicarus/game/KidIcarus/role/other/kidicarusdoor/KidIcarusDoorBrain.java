package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import java.util.Collection;

import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.playerspawner.PlayerSpawner;
import kidridicarus.game.KidIcarus.role.other.kidicarusdoor.script.KidIcarusDoorScript;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

class KidIcarusDoorBrain {
	private KidIcarusDoor parent;
	private RoleHooks parentRoleHooks;
	private KidIcarusDoorBody body;
	private boolean isOpened;
	private String exitSpawnerName;
	private boolean isTriggered;

	KidIcarusDoorBrain(KidIcarusDoor parent, RoleHooks parentRoleHooks, KidIcarusDoorBody body, boolean isOpened,
			String exitSpawnerName) {
		this.parent = parent;
		this.parentRoleHooks = parentRoleHooks;
		this.body = body;
		this.isOpened = isOpened;
		this.exitSpawnerName = exitSpawnerName;
		this.isTriggered = false;
	}

	void processContactFrame() {
		// exit if not opened, or if zero players contacting door
		if(!isOpened)
			return;
		Collection<ScriptableRole> playerContacts = body.getPlayerBeginContacts();
		if(playerContacts.isEmpty())
			return;
		// exit if spawner doesn't exist or is the wrong class
		Role exitSpawner = RP_Tool.getNamedRole(exitSpawnerName, parentRoleHooks);
		if(!(exitSpawner instanceof PlayerSpawner)) {
			throw new IllegalArgumentException("Kid Icarus Door exit spawner is not instance of "+
					PlayerSpawner.class.getName()+", exitSpawnerName="+exitSpawnerName+
					", exitSpawner="+exitSpawner);
		}
		// pass a unique door script to each player contacting this door
		for(ScriptableRole role : playerContacts) {
			// if the role is not running a script, or override of script is allowed, then create/run door script
			if(!role.scriptIsEnabled() || role.scriptIsOverrideAllowed()) {
				parentRoleHooks.storyHooks.createRole(KidIcarusDoorScript.makeRP(role, parent,
						(PlayerSpawner) exitSpawner));
			}
		}
	}

	KidIcarusDoorSpriteFrameInput processFrame() {
		if(isTriggered) {
			isTriggered = false;
			isOpened = !isOpened;
			body.setOpened(isOpened);
		}
		return new KidIcarusDoorSpriteFrameInput(body.getPosition(), isOpened);
	}

	void onTakeTrigger() {
		isTriggered = true;
	}
}
