package kidridicarus.common.role.rolespawner;

import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

class OneShotSpawnerBrain extends SpawnerBrain {
	private boolean isSpawned;

	OneShotSpawnerBrain(Role parentRole, RoleHooks parentRoleHooks, ObjectProperties properties) {
		super(parentRole, parentRoleHooks, properties);
		this.isSpawned = false;
	}

	@Override
	void update(FrameTime frameTime, boolean isEnabled) {
		if(isEnabled && !this.isSpawned) {
			this.isSpawned = true;
			doSpawn(null);
		}
	}
}
