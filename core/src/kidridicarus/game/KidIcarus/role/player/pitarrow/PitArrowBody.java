package kidridicarus.game.KidIcarus.role.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.rolesensor.SolidContactSensor;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.Role;

class PitArrowBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(3);
	private static final float BODY_HEIGHT = UInfo.P2M(3);
	private static final float GRAVITY_SCALE = 0f;
	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq RS_CFCAT = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq RS_CFMASK = new CFBitSeq(CommonCF.Alias.ROLE_BIT, CommonCF.Alias.KEEP_ALIVE_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.ROOM_BIT);

	PitArrowBody(Role parentRole, PhysicsHooks physHooks, Vector2 position, Vector2 velocity, Direction4 arrowDir,
			SolidContactSensor solidSensor, RoleContactHoldSensor roleSensor) {
		super(parentRole.getAgent(), physHooks);

		// set body size info and create new body
		if(arrowDir.isHorizontal())
			setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		// if vertical then rotate body size by 90 degrees
		else
			setBoundsSize(BODY_HEIGHT, BODY_WIDTH);
		b2body = B2DFactory.makeDynamicBody(physHooks, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		b2body.setBullet(true);
		// create main fixture
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, solidSensor, getBounds().width,
				getBounds().height);
		// create agent contact sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, RS_CFCAT, RS_CFMASK, roleSensor, getBounds().width,
				getBounds().height);
	}
}
