package kidridicarus.common.agent.agentspawntrigger;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.info.CommonCF;

class AgentSpawnTriggerBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	private OneWayContactSensor beginContactSensor;
	private OneWayContactSensor endContactSensor;

	AgentSpawnTriggerBody(AgentSpawnTrigger parent, PhysicsHooks physHooks, Rectangle bounds) {
		super(parent, physHooks, bounds, true);
		beginContactSensor = new OneWayContactSensor(parent, true);
		endContactSensor = new OneWayContactSensor(parent, false);
		beginContactSensor.chainTo(endContactSensor);
	}

	SpawnTriggerFrameInput processFrame() {
		return new SpawnTriggerFrameInput(beginContactSensor.getOnlyAndResetContacts(EnableTakeAgent.class),
				endContactSensor.getOnlyAndResetContacts(EnableTakeAgent.class));
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
