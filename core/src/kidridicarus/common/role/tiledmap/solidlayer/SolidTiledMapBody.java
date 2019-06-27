package kidridicarus.common.role.tiledmap.solidlayer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class SolidTiledMapBody extends RoleBody {
	SolidTiledMapBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		agentBody = ABodyFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				ACFB.SOLID_TILEMAP_GIVEBIT, ACFB.SOLID_TILEMAP_TAKEBIT), bounds.width, bounds.height);
	}
}
