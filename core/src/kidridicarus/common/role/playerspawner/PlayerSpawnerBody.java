package kidridicarus.common.role.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolebody.RoleBody;

class PlayerSpawnerBody extends RoleBody {
	private static final float GRAVITY_SCALE = 0f;

	PlayerSpawnerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		agentBody.setGravityScale(GRAVITY_SCALE);
		ABodyFactory.makeSensorBoxFixture(agentBody, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK,
				parentRole, bounds.width, bounds.height);
	}
}
