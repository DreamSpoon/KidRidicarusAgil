package kidridicarus.common.role.rolespawntrigger;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.role.optional.EnableTakeRole;
import kidridicarus.common.rolesensor.OneWayContactSensor;
import kidridicarus.story.Role;

class RoleSpawnTriggerBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	private OneWayContactSensor beginContactSensor;
	private OneWayContactSensor endContactSensor;

	RoleSpawnTriggerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole, physHooks, bounds, true);
		beginContactSensor = new OneWayContactSensor(parentRole, true);
		endContactSensor = new OneWayContactSensor(parentRole, false);
		beginContactSensor.chainTo(endContactSensor);
	}

	SpawnTriggerFrameInput processFrame() {
		return new SpawnTriggerFrameInput(beginContactSensor.getOnlyAndResetContacts(EnableTakeRole.class),
				endContactSensor.getOnlyAndResetContacts(EnableTakeRole.class));
	}

	@Override
	protected CFBitSeq getCatBits() {
		return CFCAT_BITS;
	}

	@Override
	protected CFBitSeq getMaskBits() {
		return CFMASK_BITS;
	}

	@Override
	protected Object getSensorBoxUserData() {
		return beginContactSensor;
	}
}
