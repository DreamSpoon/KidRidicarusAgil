package kidridicarus.common.role.scrollpushbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.tool.ABodyFactory;

class ScrollPushBoxBody extends FollowBoxBody {
	ScrollPushBoxBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks, bounds);
	}

	@Override
	protected AgentFixture createAgentFixture(Vector2 size) {
		return ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				new FilterBitSet(ACFB.SCROLL_PUSH_GIVEBIT),
				new FilterBitSet(ACFB.SCROLL_PUSH_TAKEBIT)), size);
	}
}
