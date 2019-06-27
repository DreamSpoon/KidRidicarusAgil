package kidridicarus.agency;

import java.util.HashSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.AgencyContactListener.PreSolver;
import kidridicarus.agency.tool.FilterBitSet;

public class AgentFixture {
	Fixture b2Fixture;
	AgentFilter agentFilter;
	PreSolver preSolver;
	Agent agent;
	AgentBody agentBody;
	// flag to mark if the AgentFixture has already been queued for removal
	boolean isDestroyQueueDirty;
	private HashSet<AgentFixture> beginContacts;
	private HashSet<AgentFixture> currentContacts;
	private boolean isContactDirty;
	private HashSet<AgentContactSensor> contactSensors;
	private Object userData;

	AgentFixture(Agent agent, AgentBody agentBody, Fixture b2Fixture, AgentFixtureDef afDef) {
		this.agent = agent;
		this.agentBody = agentBody;
		this.isDestroyQueueDirty = false;
		this.agentFilter = afDef.agentFilter;
		// null indicates default preSolver, non-null for custom preSolver
		this.preSolver = afDef.preSolver;
		this.beginContacts = new HashSet<AgentFixture>();
		this.currentContacts = new HashSet<AgentFixture>();
		this.isContactDirty = false;
		this.contactSensors = new HashSet<AgentContactSensor>();
		this.userData = null;
		this.b2Fixture = b2Fixture;
		this.b2Fixture.setUserData(this);
	}

	void beginContact(AgentFixture otherFixture) {
		isContactDirty = true;
		beginContacts.add(otherFixture);
		currentContacts.add(otherFixture);
		for(AgentContactSensor sensor : this.contactSensors)
			sensor.beginContact(otherFixture);
	}

	void endContact(AgentFixture otherFixture) {
		isContactDirty = true;
		currentContacts.remove(otherFixture);
		for(AgentContactSensor sensor : this.contactSensors)
			sensor.endContact(otherFixture);
	}

	void endFrameCleanContacts() {
		isContactDirty = false;
		beginContacts.clear();
		for(AgentContactSensor sensor : this.contactSensors)
			sensor.endFrameCleanSensor();
	}

	boolean isContactDirty() {
		return isContactDirty;
	}

	public AgentContactSensor createSensor(AgentFilter sensorFilter) {
		AgentContactSensor newSensor =
				new AgentContactSensor(this, sensorFilter, this.beginContacts, this.currentContacts);
		this.contactSensors.add(newSensor);
		return newSensor;
	}

	public void destroySensor(AgentContactSensor agentSensor) {
		if(!this.contactSensors.contains(agentSensor))
			throw new IllegalArgumentException("'agentSensor to destroy' is not in this fixture's list of sensors.");
		this.contactSensors.remove(agentSensor);
	}

	public void setFilterData(FilterBitSet categoryBits, FilterBitSet maskBits) {
		this.agentFilter.catBits = categoryBits;
		this.agentFilter.maskBits = maskBits;
		// filter is set when AgentFixture is created, so any changes to filter will require invoking refilter
		this.b2Fixture.refilter();
	}

	public void setFilterData(AgentFilter agentFilter) {
		this.agentFilter.catBits = agentFilter.catBits;
		this.agentFilter.maskBits = agentFilter.maskBits;
		// filter is set when AgentFixture is created, so any changes to filter will require invoking refilter
		this.b2Fixture.refilter();
	}

	public AgentFilter getFilterData() {
		return agentFilter;
	}

	public void setPreSolver(PreSolver preSolver) {
		this.preSolver = preSolver;
		// Filter is set when AgentFixture is created, so any changes to filter will require invoking refilter,
		// preSolver is closely related to filter.
		this.b2Fixture.refilter();
	}

	public PreSolver getPreSolver() {
		return preSolver;
	}

	public Agent getAgent() {
		return agent;
	}

	public AgentBody getAgentBody() {
		return agentBody;
	}

	/*
	 * References:
	 *   Get bounding box for Box2D fixture.
	 * https://gamedev.stackexchange.com/questions/80027/get-position-of-fixtures
	 *   Shape types with descriptions.
	 * https://github.com/GuidebeeGameEngine/Box2D/wiki/Shape-Types
	 */
	public Rectangle getBounds() {
		switch(b2Fixture.getShape().getType()) {
			case Circle:
				return getBoundsCircle((CircleShape) b2Fixture.getShape());
			case Edge:
				return getBoundsEdge((EdgeShape) b2Fixture.getShape());
			case Polygon:
				return getBoundsPolygon((PolygonShape) b2Fixture.getShape());
			case Chain:
				return getBoundsChain((ChainShape) b2Fixture.getShape());
			default:
				throw new IllegalStateException("Unknown Box2D fixture shape type: " + b2Fixture.getShape().getType());
		}
	}

	private Rectangle getBoundsCircle(CircleShape shape) {
		Vector2 pos = shape.getPosition();
		float r = shape.getRadius();
		return new Rectangle(pos.x - r, pos.y - r, r*2f, r*2f);
	}

	private Rectangle getBoundsEdge(EdgeShape shape) {
		// add first vertex to bounds
		Vector2 localVertex = new Vector2();
		shape.getVertex1(localVertex);
		Vector2 worldVertex = agentBody.b2body.getWorldPoint(localVertex);
		Rectangle bounds = addToBounds(null, worldVertex);
		// add second vertex to bounds and return bounds
		shape.getVertex2(localVertex);
		worldVertex = agentBody.b2body.getWorldPoint(localVertex);
		return addToBounds(bounds, worldVertex);
	}

	private Rectangle getBoundsPolygon(PolygonShape shape) {
		Rectangle bounds = null;
		Vector2 localVertex = new Vector2();
		for(int i=0; i<shape.getVertexCount(); i++) {
			shape.getVertex(i, localVertex);
			Vector2 worldVertex = agentBody.b2body.getWorldPoint(localVertex);
			bounds = addToBounds(bounds, worldVertex);
		}
		return bounds;
	}

	private Rectangle getBoundsChain(ChainShape shape) {
		Rectangle bounds = null;
		Vector2 localVertex = new Vector2();
		for(int i=0; i<shape.getVertexCount(); i++) {
			shape.getVertex(i, localVertex);
			Vector2 worldVertex = agentBody.b2body.getWorldPoint(localVertex);
			bounds = addToBounds(bounds, worldVertex);
		}
		return bounds;
	}

	private Rectangle addToBounds(Rectangle bounds, Vector2 point) {
		if(bounds == null)
			return new Rectangle(point.x, point.y, 0f, 0f);
		else {
			if(point.x < bounds.x)
				bounds.x = point.x;
			else if(point.x > bounds.x+bounds.width)
				bounds.width = point.x - bounds.x;
			if(point.y < bounds.y)
				bounds.y = point.y;
			else if(point.y > bounds.y+bounds.height)
				bounds.height = point.y - bounds.y;
		}
		return bounds;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public Object getUserData() {
		return userData;
	}
}
