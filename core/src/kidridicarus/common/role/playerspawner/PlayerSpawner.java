package kidridicarus.common.role.playerspawner;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.AgentPropertyListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class PlayerSpawner extends Role {
	public PlayerSpawner(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		final Rectangle bounds = RP_Tool.getBounds(properties);
		final String strName = properties.getString(CommonKV.Script.KEY_NAME, null);
		final String spawnType = properties.getString(CommonKV.Spawn.KEY_SPAWN_TYPE,
				CommonKV.Spawn.VAL_SPAWN_TYPE_IMMEDIATE);
		final boolean isMainSpawner = properties.getBoolean(CommonKV.Spawn.KEY_SPAWN_MAIN, false);
		final String strPlayerRoleClass = properties.getString(CommonKV.Spawn.KEY_PLAYER_ROLECLASS, null);
		final Direction4 spawnDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		// name is a global property so that this spawner can be searched, all other properties are local
		myAgentHooks.addPropertyListener(false, CommonKV.KEY_BOUNDS,
				new AgentPropertyListener<Rectangle>(Rectangle.class) {
				@Override
				public Rectangle getValue() { return bounds; }
			});
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
