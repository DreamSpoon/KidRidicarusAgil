package kidridicarus.common.role.rolespawner;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

class SingleSpawnController extends SpawnController {
	private boolean isSpawned;

	SingleSpawnController(Role parentRole, RoleHooks parentRoleHooks, ObjectProperties properties) {
		super(parentRole, parentRoleHooks, properties);
		isSpawned = false;
	}

	@Override
	void update(FrameTime frameTime, boolean isEnabled) {
		if(isEnabled && !isSpawned) {
			isSpawned = true;
			doSpawn();
		}
	}
}
