package kidridicarus.common.role.rolespawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolebody.RoleBody;

class RoleSpawnerBody extends RoleBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.ROLE_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT,
			CommonCF.Alias.SOLID_MAP_BIT);
	private static final float GRAVITY_SCALE = 0f;

	private RoleContactHoldSensor roleSensor;

	RoleSpawnerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(GRAVITY_SCALE);
		roleSensor = new RoleContactHoldSensor(parentRole);
		ABodyFactory.makeSensorBoxFixture(agentBody, CFCAT_BITS, CFMASK_BITS, roleSensor, bounds.width, bounds.height);
	}

	<T> T getFirstContactByUserDataClass(Class<T> cls) {
		return roleSensor.getFirstContactByUserDataClass(cls);
	}
}
