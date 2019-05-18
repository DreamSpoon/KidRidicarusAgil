package kidridicarus.common.role.keepalivebox;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.story.Role;

class KeepAliveBoxBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.KEEP_ALIVE_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	KeepAliveBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole, physHooks, bounds, true);
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
