package kidridicarus.common.role.despawnbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolebody.RoleBody;

class DespawnBoxBody extends RoleBody {
	private static final float GRAVITY_SCALE = 0f;
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.DESPAWN_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.ROLE_BIT);

	DespawnBoxBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(GRAVITY_SCALE);
		ABodyFactory.makeSensorBoxFixture(agentBody, CFCAT_BITS, CFMASK_BITS, parentRole,
				bounds.width, bounds.height);
	}
}
