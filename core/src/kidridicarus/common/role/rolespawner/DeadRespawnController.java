package kidridicarus.common.role.rolespawner;

import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

class DeadRespawnController extends SpawnController {
	private boolean isSpawnReset;
	private int numSpawns;
	private int numSpawnsDisposed;

	DeadRespawnController(Role parentRole, RoleHooks parentRoleHooks, ObjectProperties properties) {
		super(parentRole, parentRoleHooks, properties);
		isSpawnReset = true;
		numSpawns = 0;
		numSpawnsDisposed = 0;
	}

	@Override
	void update(FrameTime frameTime, boolean isEnabled) {
		if(!isEnabled) {
			// if all spawns have died, and the spawner is not enabled, then reset so another spawn can occur 
			if(numSpawns == numSpawnsDisposed)
				isSpawnReset = true;
		}
		else if(isSpawnAllowed()) {
			isSpawnReset = false;
			numSpawns++;
			Role spawnedRole = doSpawn();
			parentRoleHooks.agentHooksBundle.agentHooks.createAgentRemoveListener(spawnedRole.getAgent(),
					new AgentRemoveCallback() {
					@Override
					public void preRemoveAgent() { numSpawnsDisposed++; }
				});
		}
	}

	private boolean isSpawnAllowed() {
		// if the spawner has been reset and all spawns have been disposed then do respawn
		return isSpawnReset && numSpawns == numSpawnsDisposed; 
	}
}
