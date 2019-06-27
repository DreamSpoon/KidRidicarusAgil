package kidridicarus.common.role.despawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class DespawnTriggerBody extends RoleBody {
	private static final float GRAVITY_SCALE = 0f;

	DespawnTriggerBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(GRAVITY_SCALE);
		ABodyFactory.makeSensorBoxFixture(agentBody,
				new AgentFilter(new FilterBitSet(CommonCF.ACFB.DESPAWN_TRIGGER_GIVEBIT),
				new FilterBitSet(CommonCF.ACFB.DESPAWN_TRIGGER_TAKEBIT)), bounds.width, bounds.height);
	}
}
