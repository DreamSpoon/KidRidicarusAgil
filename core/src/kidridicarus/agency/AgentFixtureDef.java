package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;

import kidridicarus.agency.AgencyContactListener.PreSolver;

public class AgentFixtureDef {
	public Shape shape;
	public float friction;
	public float restitution;
	public float density;
	public boolean isSensor;
	public AgentFilter agentFilter;
	public PreSolver preSolver;

	public AgentFixtureDef() {
		FixtureDef defaultDef = new FixtureDef();
		this.shape = defaultDef.shape;
		this.friction = defaultDef.friction;
		this.restitution = defaultDef.restitution;
		this.density = defaultDef.density;
		this.isSensor = defaultDef.isSensor;
		this.agentFilter = new AgentFilter();
		this.preSolver = null;
	}

	public FixtureDef getB2FixtureDef() {
		FixtureDef outDef = new FixtureDef();
		outDef.shape = this.shape;
		outDef.friction = this.friction;
		outDef.restitution = this.restitution;
		outDef.density = this.density;
		outDef.isSensor = this.isSensor;
		return outDef;
	}
}
