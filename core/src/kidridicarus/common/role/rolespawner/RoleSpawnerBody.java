package kidridicarus.common.role.rolespawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.story.Role;

class RoleSpawnerBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT,
			CommonCF.Alias.SOLID_MAP_BIT);
	private static final float GRAVITY_SCALE = 0f;

	private RoleContactHoldSensor roleSensor;

	RoleSpawnerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole.getAgent(), physHooks);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(GRAVITY_SCALE);
		roleSensor = new RoleContactHoldSensor(parentRole);
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, roleSensor, bounds.width, bounds.height);
	}

	<T> T getFirstContactByUserDataClass(Class<T> cls) {
		return roleSensor.getFirstContactByUserDataClass(cls);
	}
}
