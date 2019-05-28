package kidridicarus.common.role.rolespawntrigger;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.rolesensor.OneWayContactSensor;

class RoleSpawnTriggerBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	RoleSpawnTriggerBody(PhysicsHooks physHooks, Rectangle bounds,
			OneWayContactSensor beginContactSensor) {
		super(physHooks, bounds, true, beginContactSensor);
	}

	@Override
	protected CFBitSeq getCatBits() {
		return CFCAT_BITS;
	}

	@Override
	protected CFBitSeq getMaskBits() {
		return CFMASK_BITS;
	}
}
