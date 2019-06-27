package kidridicarus.common.role.onewayfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.rolebody.RoleBody;

class OneWayFloorBody extends RoleBody {
	OneWayFloorBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		agentBody = ABodyFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		// Fixture 1) original conditional solid part of the body; uses a pre-solver to conditionally allow movement
		// up and not down.
		AgentFixture condSolidFixture = ABodyFactory.makeBoxFixture(agentBody,
				new AgentFilter(new FilterBitSet(CommonCF.ACFB.COND_SOLID_GIVEBIT),
				new FilterBitSet(CommonCF.ACFB.COND_SOLID_TAKEBIT)), bounds.width, bounds.height);
		// pre-solver is attached only to fixture 1
		OneWayFloorPreSolver floorPreSolver = new OneWayFloorPreSolver();
		condSolidFixture.setPreSolver(floorPreSolver);
		// Fixture 2) "always" solid part of the body, with different contact filter bit set and no pre-solver
		AgentFixture alwaysFixture = ABodyFactory.makeBoxFixture(agentBody,
				new AgentFilter(new FilterBitSet(CommonCF.ACFB.COND_SOLID_ALWAYS_GIVEBIT),
				new FilterBitSet(CommonCF.ACFB.COND_SOLID_ALWAYS_TAKEBIT)), bounds.width, bounds.height);
		// userData refers to the pre-solver on the conditional solid part of the body, so users can check other
		// boundary sets with the preSolver - e.g. for on-ground detection. This fixture exactly overlaps original
		// conditional solid fixture, so preSolver use with this fixture will return results exactly the same as
		// with the original conditional solid fixture.
		alwaysFixture.setUserData(floorPreSolver);
	}
}
