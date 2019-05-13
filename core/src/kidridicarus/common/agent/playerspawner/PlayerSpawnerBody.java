package kidridicarus.common.agent.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

class PlayerSpawnerBody extends AgentBody {
	private static final float GRAVITY_SCALE = 0f;

	PlayerSpawnerBody(PhysicsHooks physHooks, PlayerSpawner parent, Rectangle bounds) {
		super(parent, physHooks);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				bounds.width, bounds.height);
	}
}
