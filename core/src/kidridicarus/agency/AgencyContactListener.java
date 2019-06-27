package kidridicarus.agency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/*
 * Use a custom filtering (AgentBodyFilter) method to determine contact. When contact occurs, invoke the
 * sensor contact methods. Also use contact.isTouching() for more precise contact detection. To implement this,
 * it is necessary to keep a list of all active contacts based on their (fixtureA, fixtureB) pair - note that the
 * pair (fixtureA, fixtureB) is equivalent to the pair (fixtureB, fixtureA).
 * From debugging experience, I've learned that the same Contact object is passed as a parameter for every call
 * to beginContact and endContact. So a workaround was necessary...
 * Treating each pair of fixtures in the contact as a single meta-object (by using the Objects.hash method)
 * allows use of a HashMap to keep a list of current contacts with their isTouching states (since each contact
 * is unique to it's (fixtureA, fixtureB) pair, but the pairs may be given in reverse order).
 * For info on Objects.hash see:
 *   https://stackoverflow.com/questions/11597386/objects-hash-vs-objects-hashcode-clarification-needed
 * Hash collisions may still result, so:
 *   TODO Since all hash calculations are performed in this one class, then hash collisions can also be resolved here.
 *
 * A custom preSolver can be used to filter semi-solids (e.g. one-way floors). If the custom pre-solver is null
 * then the default preSolver (default for Agency, anyways) will be used.
 */
public class AgencyContactListener implements ContactListener {
	public interface PreSolver { public boolean preSolve(AgentContactHalf yourHalf, AgentContactHalf otherHalf); }

	private HashMap<Integer, Boolean> contactActuals;
	private HashSet<AgentFixture> dirtyContactFixtures;

	AgencyContactListener() {
		contactActuals = new HashMap<Integer, Boolean>();
		dirtyContactFixtures = new HashSet<AgentFixture>();
	}

	@Override
	public void beginContact(Contact contact) {
		// Use a hash value combo of fixtures A and B to index, because each contact is unique to the
		// combo of fixtures A and B.
		int hashAB = Objects.hash(contact.getFixtureA(), contact.getFixtureB());
		contactActuals.put(hashAB, contact.isTouching());
		// if actually touching on first contact then do actual begin
		if(contact.isTouching())
			actualBeginContact(contact);
	}

	@Override
	public void endContact(Contact contact) {
		int hashAB = Objects.hash(contact.getFixtureA(), contact.getFixtureB());
		Boolean wasTouching = contactActuals.get(hashAB);
		contactActuals.remove(hashAB);
		// if actually touching on last contact then do actual end
		if(wasTouching)
			actualEndContact(contact);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		boolean isEnabledA = true;
		boolean isEnabledB = true;
		AgentFixture agentFixtureA = (AgentFixture) contact.getFixtureA().getUserData();
		AgentFixture agentFixtureB = (AgentFixture) contact.getFixtureB().getUserData();
		AgentContactHalf contactHalfA = new AgentContactHalf(agentFixtureA.agent, agentFixtureA.agentBody, agentFixtureA);
		AgentContactHalf contactHalfB = new AgentContactHalf(agentFixtureB.agent, agentFixtureB.agentBody, agentFixtureB);
		// if fixtureA has a custom preSolver then use it
		if(agentFixtureA.preSolver != null)
			isEnabledA = agentFixtureA.preSolver.preSolve(contactHalfA, contactHalfB);
		// if fixtureB has a custom preSolver then use it
		if(agentFixtureB.preSolver != null)
			isEnabledB = agentFixtureB.preSolver.preSolve(contactHalfB, contactHalfA);
		// apply results of custom presolvers (if no presolvers exist then this will always setEnabled(true) )
		contact.setEnabled(isEnabledA && isEnabledB);
	}

	// check for and do actual contact changes
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		int hashAB = Objects.hash(contact.getFixtureA(), contact.getFixtureB());
		Boolean wasTouching = contactActuals.get(hashAB);
		if(!wasTouching && contact.isTouching()) {
			contactActuals.put(hashAB, true);
			actualBeginContact(contact);
		}
		else if(wasTouching && !contact.isTouching()) {
			contactActuals.put(hashAB, false);
			actualEndContact(contact);
		}
	}

	private void actualBeginContact(Contact contact) {
		AgentFixture agentFixtureA = (AgentFixture) contact.getFixtureA().getUserData();
		AgentFixture agentFixtureB = (AgentFixture) contact.getFixtureB().getUserData();
		AgentContact agentContact = new AgentContact(agentFixtureA.agent, agentFixtureB.agent,
				agentFixtureA.agentBody, agentFixtureB.agentBody, agentFixtureA, agentFixtureB);
		boolean isPrevDirtyA =  agentFixtureA.isContactDirty();
		boolean isPrevDirtyB =  agentFixtureB.isContactDirty();
		agentFixtureA.beginContact(agentContact.agentFixtureB);
		agentFixtureB.beginContact(agentContact.agentFixtureA);
		// add fixture A to list of dirty contacts if fixture A changed to dirty due to begin contact
		if(!isPrevDirtyA && agentFixtureA.isContactDirty())
			dirtyContactFixtures.add(agentFixtureA);
		// add fixture B to list of dirty contacts if fixture B changed to dirty due to begin contact
		if(!isPrevDirtyB && agentFixtureB.isContactDirty())
			dirtyContactFixtures.add(agentFixtureB);
	}

	private void actualEndContact(Contact contact) {
		AgentFixture agentFixtureA = (AgentFixture) contact.getFixtureA().getUserData();
		AgentFixture agentFixtureB = (AgentFixture) contact.getFixtureB().getUserData();
		AgentContact agentContact = new AgentContact(agentFixtureA.agent, agentFixtureB.agent,
				agentFixtureA.agentBody, agentFixtureB.agentBody, agentFixtureA, agentFixtureB);
		boolean isPrevDirtyA =  agentFixtureA.isContactDirty();
		boolean isPrevDirtyB =  agentFixtureB.isContactDirty();
		agentFixtureA.endContact(agentContact.agentFixtureB);
		agentFixtureB.endContact(agentContact.agentFixtureA);
		// add fixture A to list of dirty contacts if fixture A changed to dirty due to end contact
		if(!isPrevDirtyA && agentFixtureA.isContactDirty())
			dirtyContactFixtures.add(agentFixtureA);
		// add fixture B to list of dirty contacts if fixture B changed to dirty due to end contact
		if(!isPrevDirtyB && agentFixtureB.isContactDirty())
			dirtyContactFixtures.add(agentFixtureB);
	}

	void endFrameCleanContactSensors() {
		Iterator<AgentFixture> fixIter = dirtyContactFixtures.iterator();
		while(fixIter.hasNext())
			fixIter.next().endFrameCleanContacts();
		dirtyContactFixtures.clear();
	}
}
