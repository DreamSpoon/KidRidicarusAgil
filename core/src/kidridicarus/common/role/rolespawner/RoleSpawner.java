package kidridicarus.common.role.rolespawner;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class RoleSpawner extends Role implements EnableTakeRole {
	private SpawnerBrain brain;
	private boolean isEnabled;

	public RoleSpawner(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		// verify that the class of the Role to be spawned is a valid Role class
		String spawnRoleClassAlias = properties.getString(CommonKV.Spawn.KEY_SPAWN_ROLECLASS, "");
		if(!roleHooks.storyHooks.isValidRoleClassAlias(spawnRoleClassAlias)) {
			throw new IllegalStateException(
					"Cannot create RoleSpawner with non-valid Role class alias =" + spawnRoleClassAlias);
		}
		this.isEnabled = false;
		RoleSpawnerBody body = new RoleSpawnerBody(myPhysHooks, RP_Tool.getBounds(properties));
		// create controller for the desired type of spawning
		String spawnerType = properties.getString(CommonKV.Spawn.KEY_SPAWNER_TYPE, "");
		if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_RESPAWN))
			this.brain = new DeadRespawnSpawnerBrain(this, roleHooks, properties);
		else if(spawnerType.equals(CommonKV.Spawn.VAL_SPAWNER_TYPE_MULTI))
			this.brain = new MultiShotSpawnerBrain(this, roleHooks, properties, (RoleSpawnerBody) body);
		else
			this.brain = new OneShotSpawnerBrain(this, roleHooks, properties);

		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) {
				if(brain != null)
					brain.update(frameTime, isEnabled);
			}
		});
	}

	@Override
	public void onTakeEnable(boolean enabled) {
		this.isEnabled = enabled;
	}
}
