package kidridicarus.common.metarole.tiledmap.solidlayer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolebody.RoleBody;

class SolidTiledMapBody extends RoleBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SOLID_MAP_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	SolidTiledMapBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		ABodyFactory.makeSensorBoxFixture(agentBody, CFCAT_BITS, CFMASK_BITS, parentRole,
				bounds.width, bounds.height);
	}
}
