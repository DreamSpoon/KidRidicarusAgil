package kidridicarus.common.sensor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.story.RoleSensor;

// parent role "takes" despawn trigger and keep alive trigger contacts 
public class SolidTileMapSensor extends RoleSensor {
	public SolidTileMapSensor(AgentFixture agentFixture) {
		super(agentFixture, new AgentFilter(ACFB.SOLID_TILEMAP_TAKEBIT, ACFB.SOLID_TILEMAP_GIVEBIT));
	}

	boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapRole ctMap = getFirstCurrentContactByRoleClass(SolidTiledMapRole.class);
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}

	boolean isMapPointSolid(Vector2 position) {
		SolidTiledMapRole ctMap = getFirstCurrentContactByRoleClass(SolidTiledMapRole.class);
		return ctMap == null ? false : ctMap.isMapPointSolid(position); 
	}
}
