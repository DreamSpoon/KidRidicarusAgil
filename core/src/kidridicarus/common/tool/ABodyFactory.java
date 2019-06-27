package kidridicarus.common.tool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.AgentBody;
import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.AgentFixtureDef;
import kidridicarus.agency.PhysicsHooks;

/*
 * Convenience class for Agent body/fixture creation.
 */
public class ABodyFactory {
	public static AgentBody makeDynamicBody(PhysicsHooks physHooks, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		return physHooks.createAgentBody(bdef);
	}

	public static AgentBody makeDynamicBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		if(velocity != null)
			bdef.linearVelocity.set(velocity);
		return physHooks.createAgentBody(bdef);
	}

	public static AgentBody makeStaticBody(PhysicsHooks physHooks, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.set(position);
		return physHooks.createAgentBody(bdef);
	}

	public static AgentFixture makeBoxFixture(AgentBody agentBody, AgentFixtureDef afDef, float width, float height,
			Vector2 position) {
		afDef.shape = new PolygonShape();
		((PolygonShape) afDef.shape).setAsBox(width/2f, height/2f, position, 0f);
		return agentBody.createFixture(afDef);
	}

	public static AgentFixture makeBoxFixture(AgentBody agentBody, AgentFixtureDef afDef, float width, float height) {
		afDef.shape = new PolygonShape();
		((PolygonShape) afDef.shape).setAsBox(width/2f, height/2f);
		return agentBody.createFixture(afDef);
	}

	public static AgentFixture makeBoxFixture(AgentBody agentBody, AgentFixtureDef afDef, Vector2 size) {
		return makeBoxFixture(agentBody, afDef, size.x, size.y);
	}

	public static AgentFixture makeBoxFixture(AgentBody agentBody, AgentFilter agentFilter, Vector2 size) {
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.agentFilter = agentFilter;
		return makeBoxFixture(agentBody, afDef, size.x, size.y);
	}

	public static AgentFixture makeBoxFixture(AgentBody agentBody, AgentFilter agentFilter, float width,
			float height) {
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.agentFilter = agentFilter;
		return makeBoxFixture(agentBody, afDef, width, height);
	}

	public static AgentFixture makeBoxFixture(AgentBody agentBody, AgentFilter agentFilter, Vector2 size,
			Vector2 position) {
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.agentFilter = agentFilter;
		return makeBoxFixture(agentBody, afDef, size.x, size.y, position);
	}

	public static AgentFixture makeSensorBoxFixture(AgentBody agentBody, AgentFilter agentFilter, Vector2 size) {
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.isSensor = true;
		afDef.agentFilter = agentFilter;
		return makeBoxFixture(agentBody, afDef, size.x, size.y);
	}

	public static AgentFixture makeSensorBoxFixture(AgentBody agentBody, AgentFilter agentFilter, float width,
			float height) {
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.isSensor = true;
		afDef.agentFilter = agentFilter;
		return makeBoxFixture(agentBody, afDef, width, height);
	}

	public static AgentFixture makeSensorBoxFixture(AgentBody agentBody, AgentFilter agentFilter, Vector2 size,
			Vector2 position) {
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.isSensor = true;
		afDef.agentFilter = agentFilter;
		return makeBoxFixture(agentBody, afDef, size.x, size.y, position);
	}
}
