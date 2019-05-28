package kidridicarus.game.KidIcarus.role.NPC.shemum;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.rolesensor.SolidContactSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class ShemumBody extends RoleBody {
	private static final float BODY_WIDTH = UInfo.P2M(6f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = BODY_WIDTH;
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);
	private static final float PLAYER_SENSOR_WIDTH = UInfo.P2M(256);
	private static final float PLAYER_SENSOR_HEIGHT = UInfo.P2M(256);
	private static final Vector2 PLAYER_SENSOR_OFFSET = UInfo.VectorP2M(0f, -80);
	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq RS_CFCAT = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq RS_CFMASK = new CFBitSeq(CommonCF.Alias.ROLE_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.ROOM_BIT);

	ShemumBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity, SolidContactSensor solidSensor,
			RoleContactHoldSensor roleSensor, RoleContactHoldSensor playerSensor) {
		super(physHooks);
		// set body size info and create new body
		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, position, velocity);
		// main body fixture
		ABodyFactory.makeBoxFixture(agentBody, MAIN_CFCAT, MAIN_CFMASK, solidSensor, getBounds().width,
				getBounds().height);
		// role sensor fixture
		ABodyFactory.makeSensorBoxFixture(agentBody, RS_CFCAT, RS_CFMASK, roleSensor, getBounds().width,
				getBounds().height);
		// ground sensor fixture
		ABodyFactory.makeSensorBoxFixture(agentBody, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, solidSensor,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBounds().height/2f));
		// create player sensor fixture that covers most of the screen and detects players to target
		ABodyFactory.makeSensorBoxFixture(agentBody, CommonCF.ROLE_SENSOR_CFCAT, CommonCF.ROLE_SENSOR_CFMASK,
				playerSensor, PLAYER_SENSOR_WIDTH, PLAYER_SENSOR_HEIGHT, PLAYER_SENSOR_OFFSET);
	}
}
