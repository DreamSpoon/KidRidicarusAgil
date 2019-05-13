package kidridicarus.common.agent.scrollpushbox;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.info.CommonCF;

class ScrollPushBoxBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SCROLL_PUSH_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.AGENT_BIT);

	ScrollPushBoxBody(FollowBox parent, PhysicsHooks physHooks, Rectangle bounds) {
		super(parent, physHooks, bounds, false);
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
		return this;
	}
}
