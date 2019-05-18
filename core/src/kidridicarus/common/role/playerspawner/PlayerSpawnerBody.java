package kidridicarus.common.role.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.story.Role;

class PlayerSpawnerBody extends AgentBody {
	private static final float GRAVITY_SCALE = 0f;

	PlayerSpawnerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole.getAgent(), physHooks);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				bounds.width, bounds.height);
	}
}
