package kidridicarus.common.role.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF.ACFB;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class RoomBoxBody extends RoleBody {
	private static final float GRAVITY_SCALE = 0;

	private Vector2 boundSize;

	RoomBoxBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		this.boundSize = bounds.getSize(new Vector2());
		this.agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		this.agentBody.setGravityScale(GRAVITY_SCALE);
		ABodyFactory.makeSensorBoxFixture(agentBody, new AgentFilter(
				new FilterBitSet(ACFB.ROOM_GIVEBIT), new FilterBitSet(ACFB.ROOM_TAKEBIT)),
				bounds.width, bounds.height);
	}

	float getLeftX() {
		return agentBody.getPosition().x - boundSize.x/2f;
	}

	float getRightX() {
		return agentBody.getPosition().x + boundSize.x/2f;
	}

	float getBottomY() {
		return agentBody.getPosition().y - boundSize.y/2f;
	}

	float getTopY() {
		return agentBody.getPosition().y + boundSize.y/2f;
	}
}
