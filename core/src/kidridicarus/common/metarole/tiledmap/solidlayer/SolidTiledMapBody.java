package kidridicarus.common.metarole.tiledmap.solidlayer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.story.Role;

class SolidTiledMapBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SOLID_MAP_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	SolidTiledMapBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole.getAgent(), physHooks);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, this, bounds.width, bounds.height);
	}
}
