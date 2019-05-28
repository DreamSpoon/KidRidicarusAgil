package kidridicarus.common.role.scrollpushbox;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.story.Role;

class ScrollPushBoxBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SCROLL_PUSH_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.ROLE_BIT);

	ScrollPushBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks, bounds, false, parentRole);
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
