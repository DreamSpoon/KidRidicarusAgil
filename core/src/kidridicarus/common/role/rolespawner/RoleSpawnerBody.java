package kidridicarus.common.role.rolespawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.rolespawntrigger.RoleSpawnTrigger;
import kidridicarus.common.role.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;

class RoleSpawnerBody extends RoleBody {
	private static final float GRAVITY_SCALE = 0f;
	private RoleSensor spawnTriggerSensor;
	private RoleSensor tileMapSensor;

	RoleSpawnerBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(GRAVITY_SCALE);
		AgentFixture myFixture = ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				new FilterBitSet(ACFB.SPAWN_TRIGGER_TAKEBIT, ACFB.SOLID_TILEMAP_TAKEBIT),
				new FilterBitSet(ACFB.SPAWN_TRIGGER_GIVEBIT, ACFB.SOLID_TILEMAP_GIVEBIT)),
				bounds.width, bounds.height);
		spawnTriggerSensor = new RoleSensor(myFixture, new AgentFilter(
				new FilterBitSet(ACFB.SPAWN_TRIGGER_TAKEBIT), new FilterBitSet(ACFB.SPAWN_TRIGGER_GIVEBIT)));
		tileMapSensor = new RoleSensor(myFixture, new AgentFilter(
				new FilterBitSet(ACFB.SOLID_TILEMAP_TAKEBIT), new FilterBitSet(ACFB.SOLID_TILEMAP_GIVEBIT)));
	}

	public RoleSpawnTrigger getRoleSpawnTrigger() {
		return spawnTriggerSensor.getFirstCurrentContactByRoleClass(RoleSpawnTrigger.class);
	}

	public SolidTiledMapRole getSolidTiledMap() {
		return tileMapSensor.getFirstCurrentContactByRoleClass(SolidTiledMapRole.class);
	}
}
