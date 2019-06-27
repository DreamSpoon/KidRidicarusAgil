package kidridicarus.common.role.onewayfloor;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.AgencyContactListener.PreSolver;
import kidridicarus.agency.AgentContactHalf;
import kidridicarus.common.info.UInfo;

/*
 * Floor is solid if bottom of other's fixture is above top of floor's fixture.
 * Floor is non-solid if bottom of other's fixture is below top of floor's fixture.
 * Epsilon makes it easier for fixtures to contact. i.e. Floor can be solid while other fixture's bounds
 * slightly overlap floor's bounds.
 */
class OneWayFloorPreSolver implements PreSolver {
	@Override
	public boolean preSolve(AgentContactHalf yourHalf, AgentContactHalf otherHalf) {
		Rectangle myBounds = yourHalf.agentFixture.getBounds();
		Rectangle otherBounds = otherHalf.agentFixture.getBounds();
		// if other fixture is above my fixture then collision is possible
		return otherBounds.y >= myBounds.y + myBounds.height - UInfo.POS_EPSILON;
	}
}
