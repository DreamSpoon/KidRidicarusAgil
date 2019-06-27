package kidridicarus.common.role.rolespawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

abstract class SpawnerBrain {
	private Role parentRole;
	private RoleHooks parentRoleHooks;
	private String spawnRoleClassAlias;
	private Boolean isRandomPos;

	abstract void update(FrameTime frameTime, boolean isEnabled);

	SpawnerBrain(Role parentRole, RoleHooks parentRoleHooks, ObjectProperties properties) {
		this.parentRole = parentRole;
		this.parentRoleHooks = parentRoleHooks;
		this.spawnRoleClassAlias = properties.getString(CommonKV.Spawn.KEY_SPAWN_ROLECLASS, "");
		// spawn in random position within spawn body boundaries?
		isRandomPos = properties.getBoolean(CommonKV.Spawn.KEY_SPAWN_RAND_POS, false);
	}

	Role doSpawn(Vector2 position, AgentRemovalCallback removalCallback) {
		Role spawnedRole = parentRoleHooks.storyHooks.createRole(RP_Tool.createPointRP(spawnRoleClassAlias, position));
		if(removalCallback != null) {
			parentRoleHooks.agentHooksBundle.agentHooks.createExternalRemovalListener(spawnedRole.getAgent(),
					removalCallback);
		}
		return spawnedRole;
	}

	Role doSpawn(AgentRemovalCallback removalCallback) {
		// get spawn position and exit if unavailable
		Vector2 spawnPos = RP_Tool.getCenter(parentRole);
		if(spawnPos == null)
			return null;
		// apply random positioning if needed and available
		if(isRandomPos) {
			Rectangle spawnBounds = RP_Tool.getBounds(parentRole);
			if(spawnBounds != null) {
				spawnPos = new Vector2((float) (spawnBounds.x + spawnBounds.width * Math.random()),
						(float) (spawnBounds.y + spawnBounds.height * Math.random()));
			}
		}
		return doSpawn(spawnPos, removalCallback);
	}
}
