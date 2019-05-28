package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class KidIcarusDoorBody extends RoleBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(27f);
	private static final Vector2 BODY_OFFSET = UInfo.VectorP2M(0f, -2.5f);
	private static final float ROOF_WIDTH = UInfo.P2M(13f);
	private static final float ROOF_HEIGHT = UInfo.P2M(4f);

	private static final CFBitSeq CLOSED_CFCAT = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);
	private static final CFBitSeq CLOSED_CFMASK = new CFBitSeq(true);
	private static final CFBitSeq OPENED_CFCAT = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq OPENED_CFMASK = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq ROOF_CFCAT = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);
	private static final CFBitSeq ROOF_CFMASK = new CFBitSeq(true);

	private RoleContactHoldSensor roleSensor;
	private Fixture mainBodyFixture;

	KidIcarusDoorBody(PhysicsHooks physHooks, Vector2 position, boolean isOpened,
			RoleContactHoldSensor roleSensor) {
		super(physHooks);
		this.roleSensor = roleSensor;

		// set body size and create new body
		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		agentBody = ABodyFactory.makeStaticBody(physHooks, position.cpy().add(BODY_OFFSET));
		// create the Role sensor, it will be used now and/or later
		mainBodyFixture = ABodyFactory.makeBoxFixture(agentBody, isOpened ? OPENED_CFCAT : CLOSED_CFCAT,
				isOpened ? OPENED_CFMASK : CLOSED_CFMASK, roleSensor, getBounds().width, getBounds().height);
		// solid roof that player can stand on
		ABodyFactory.makeBoxFixture(agentBody, ROOF_CFCAT, ROOF_CFMASK, this, ROOF_WIDTH, ROOF_HEIGHT,
				new Vector2(0f, (BODY_HEIGHT+ROOF_HEIGHT)/2f));
	}

	void setOpened(boolean isOpened) {
		mainBodyFixture.setUserData(new AgentBodyFilter(isOpened ? OPENED_CFCAT : CLOSED_CFCAT,
				isOpened ? OPENED_CFMASK : CLOSED_CFMASK, roleSensor));
		mainBodyFixture.refilter();
	}
}
