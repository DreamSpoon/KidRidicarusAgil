package kidridicarus.common.role.playerspawner;

import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.RoleHooks;

public class PlayerSpawner extends CorpusRole {
	public PlayerSpawner(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		body = new PlayerSpawnerBody(this, myPhysHooks, RP_Tool.getBounds(properties));
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});

		final String strName = properties.getString(CommonKV.Script.KEY_NAME, null);
		final String spawnType = properties.getString(CommonKV.Spawn.KEY_SPAWN_TYPE,
				CommonKV.Spawn.VAL_SPAWN_TYPE_IMMEDIATE);
		final boolean isMainSpawner = properties.getBoolean(CommonKV.Spawn.KEY_SPAWN_MAIN, false);
		final String strPlayerRoleClass = properties.getString(CommonKV.Spawn.KEY_PLAYER_ROLECLASS, null);
		final Direction4 spawnDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		// name is a global property so that this spawner can be searched, all other properties are local
		myAgentHooks.addPropertyListener(true, CommonKV.Script.KEY_NAME,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return strName; }
			});
		myAgentHooks.addPropertyListener(false, CommonKV.Spawn.KEY_SPAWN_TYPE,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return spawnType; }
			});
		myAgentHooks.addPropertyListener(true, CommonKV.Spawn.KEY_SPAWN_MAIN,
				new AgentPropertyListener<Boolean>(Boolean.class) {
				@Override
				public Boolean getValue() { return isMainSpawner; }
			});
		myAgentHooks.addPropertyListener(false, CommonKV.Spawn.KEY_PLAYER_ROLECLASS,
				new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return strPlayerRoleClass; }
			});
		myAgentHooks.addPropertyListener(false, CommonKV.KEY_DIRECTION,
				new AgentPropertyListener<Direction4>(Direction4.class) {
				@Override
				public Direction4 getValue() { return spawnDir; }
			});
	}
}
