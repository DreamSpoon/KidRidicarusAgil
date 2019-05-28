package kidridicarus.common.role.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolebody.RoleBody;

class RoomBoxBody extends RoleBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.ROOM_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);
	private static final float GRAVITY_SCALE = 0;

	RoomBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(GRAVITY_SCALE);
		ABodyFactory.makeSensorBoxFixture(agentBody, CFCAT_BITS, CFMASK_BITS, parentRole,
				bounds.width, bounds.height);
	}
}
