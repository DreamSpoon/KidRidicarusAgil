package kidridicarus.game.KidIcarus.role.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class AngelHeartBody extends RoleBody {
	private static final float BODY_WIDTH = UInfo.P2M(3f);
	private static final float BODY_HEIGHT = UInfo.P2M(3f);

	AngelHeartBody(PhysicsHooks physHooks, Vector2 position, RoleContactHoldSensor roleSensor) {
		super(physHooks);
		// set body size info and create new body
		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, position);
		agentBody.setGravityScale(0f);
		// role sensor fixture
		ABodyFactory.makeSensorBoxFixture(agentBody, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, roleSensor,
				getBounds().width, getBounds().height);
	}
}
