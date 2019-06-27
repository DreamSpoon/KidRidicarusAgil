package kidridicarus.common.role.rolespawner;

import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

class DeadRespawnSpawnerBrain extends SpawnerBrain {
	private boolean isSpawnReset;
	private int numSpawns;
	private int numSpawnsDisposed;

	DeadRespawnSpawnerBrain(Role parentRole, RoleHooks parentRoleHooks, ObjectProperties properties) {
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
			return;
		}
		if(!isSpawnAllowed())
			return;
		isSpawnReset = false;
		numSpawns++;
		doSpawn(new AgentRemovalCallback() {
				@Override
				public void preAgentRemoval() { numSpawnsDisposed++; }
				@Override
				public void postAgentRemoval() {}	// this method is purposely empty
			});
	}

	private boolean isSpawnAllowed() {
		// if the spawner has been reset and all spawns have been disposed then do respawn
		return isSpawnReset && numSpawns == numSpawnsDisposed; 
	}
}
