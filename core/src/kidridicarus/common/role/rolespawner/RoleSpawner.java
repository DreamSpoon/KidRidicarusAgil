package kidridicarus.common.role.rolespawner;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class RoleSpawner extends CorpusRole implements EnableTakeRole {
	private SpawnController spawnController;
	private boolean isEnabled;

	public RoleSpawner(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		// verify that the class of the Role to be spawned is a valid Role class
		String spawnRoleClassAlias = properties.getString(CommonKV.Spawn.KEY_SPAWN_ROLECLASS, "");
		if(!roleHooks.storyHooks.isValidRoleClassAlias(spawnRoleClassAlias)) {
			throw new IllegalStateException(
					"Cannot create RoleSpawner with non-valid Role class alias =" + spawnRoleClassAlias);
		}
		isEnabled = false;
		body = new RoleSpawnerBody(this, myPhysHooks, RP_Tool.getBounds(properties));
		// create controller for the desired type of spawning
		String spawnerType = properties.getString(CommonKV.Spawn.KEY_SPAWNER_TYPE, "");
		if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_MULTI))
			spawnController = new MultiSpawnController(this, roleHooks, properties, (RoleSpawnerBody) body);
		else if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_RESPAWN))
			spawnController = new DeadRespawnController(this, roleHooks, properties);
		else
			spawnController = new SingleSpawnController(this, roleHooks, properties);

		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) {
				if(spawnController != null)
					spawnController.update(frameTime, isEnabled);
			}
		});
		myAgentHooks.createInternalRemovalListener(new AgentRemovalCallback() {
			@Override
			public void preAgentRemoval() { dispose(); }
			@Override
			public void postAgentRemoval() {}
		});
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		this.isEnabled = enabled;
	}
}
