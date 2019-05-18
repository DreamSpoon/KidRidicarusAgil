package kidridicarus.game.KidIcarus.role.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.story.Role;

class AngelHeartBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(3f);
	private static final float BODY_HEIGHT = UInfo.P2M(3f);

	AngelHeartBody(Role parentRole, PhysicsHooks physHooks, Vector2 position, RoleContactHoldSensor roleSensor) {
		super(parentRole.getAgent(), physHooks);
		// set body size info and create new body
		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(physHooks, position);
		b2body.setGravityScale(0f);
		// role sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, roleSensor,
				getBounds().width, getBounds().height);
	}
}
