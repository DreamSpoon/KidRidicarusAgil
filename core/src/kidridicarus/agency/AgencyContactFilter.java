package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.Fixture;

/*
 * Implement an infinite bit contact filter scheme by way of the enum ContactBit and the class CustomFilter.
 */
public class AgencyContactFilter implements ContactFilter {
	@Override
	public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
		AgentFixture tempA = (AgentFixture) fixtureA.getUserData();
		AgentFixture tempB = (AgentFixture) fixtureB.getUserData();
		return AgentFilter.shouldCollide(tempA.agentFilter, tempB.agentFilter);
	}
}
