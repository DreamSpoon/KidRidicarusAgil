package kidridicarus.game.KidIcarus.agent.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

class AngelHeartBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(3f);
	private static final float BODY_HEIGHT = UInfo.P2M(3f);

	AngelHeartBody(AngelHeart parent, PhysicsHooks physHooks, Vector2 position, AgentContactHoldSensor agentSensor) {
		super(parent, physHooks);
		// set body size info and create new body
		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(physHooks, position);
		b2body.setGravityScale(0f);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, agentSensor,
				getBounds().width, getBounds().height);
	}
}
