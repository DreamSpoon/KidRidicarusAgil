package kidridicarus.common.role.despawnbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.story.Role;

class DespawnBoxBody extends AgentBody {
	private static final float GRAVITY_SCALE = 0f;
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.DESPAWN_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.ROLE_BIT);

	DespawnBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole.getAgent(), physHooks);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, this, bounds.width, bounds.height);
	}
}
