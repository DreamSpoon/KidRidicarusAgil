package kidridicarus.common.role.scrollkillbox;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.story.Role;

class ScrollKillBoxBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SCROLL_KILL_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.ROLE_BIT);

	ScrollKillBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
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
