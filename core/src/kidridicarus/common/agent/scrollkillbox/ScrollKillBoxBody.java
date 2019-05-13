package kidridicarus.common.agent.scrollkillbox;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.info.CommonCF;

class ScrollKillBoxBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SCROLL_KILL_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.AGENT_BIT);

	ScrollKillBoxBody(FollowBox parent, PhysicsHooks physHooks, Rectangle bounds) {
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
