package kidridicarus.common.role.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class SemiSolidFloorBody extends RoleBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SEMISOLID_FLOOR_BIT,
			CommonCF.Alias.SEMISOLID_FLOOR_FOOT_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	SemiSolidFloorBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		AgentBodyFilter abf = new AgentBodyFilter(CFCAT_BITS, CFMASK_BITS, this);
		abf.preSolver = new SemiSolidPreSolver(abf);
		ABodyFactory.makeBoxFixture(agentBody, abf, getBounds().width, getBounds().height);
	}
}
