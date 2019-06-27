package kidridicarus.common.role.scrollkillbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.role.followbox.FollowBoxBody;
import kidridicarus.common.tool.ABodyFactory;

class ScrollKillBoxBody extends FollowBoxBody {
	ScrollKillBoxBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks, bounds);
	}

	@Override
	protected AgentFixture createAgentFixture(Vector2 size) {
		return ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				new FilterBitSet(ACFB.DESPAWN_TRIGGER_GIVEBIT),
				new FilterBitSet(ACFB.DESPAWN_TRIGGER_TAKEBIT)), size);
	}
}
