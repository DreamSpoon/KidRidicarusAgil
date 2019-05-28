package kidridicarus.common.tool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.AgentBody;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;

/*
 * Convenience class for Agent body/fixture creation.
 */
public class ABodyFactory {
	public static AgentBody makeDynamicBody(PhysicsHooks physHooks, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		return physHooks.createBody(bdef);
	}

	public static AgentBody makeDynamicBody(PhysicsHooks physHooks, Vector2 position, Vector2 velocity) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		if(velocity != null)
			bdef.linearVelocity.set(velocity);
		return physHooks.createBody(bdef);
	}

	public static AgentBody makeStaticBody(PhysicsHooks physHooks, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.set(position);
		return physHooks.createBody(bdef);
	}

	private static Fixture makeBoxFixture(AgentBody agentBody, FixtureDef fdef, AgentBodyFilter abFilter,
			float width, float height, Vector2 position) {
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(width/2f, height/2f, position, 0f);
		fdef.shape = boxShape;
		Fixture fix = agentBody.createFixture(fdef);
		fix.setUserData(abFilter);
		return fix;
	}

	public static Fixture makeBoxFixture(AgentBody agentBody, CFBitSeq categoryBits, CFBitSeq maskBits, Object userData,
			float width, float height) {
		return makeBoxFixture(agentBody, new FixtureDef(),
				new AgentBodyFilter(categoryBits, maskBits, userData), width, height, new Vector2(0f, 0f));
	}

	public static Fixture makeBoxFixture(AgentBody agentBody, CFBitSeq categoryBits, CFBitSeq maskBits, Object userData,
			float width, float height, Vector2 position) {
		return makeBoxFixture(agentBody, new FixtureDef(), new AgentBodyFilter(categoryBits, maskBits, userData),
				width, height, position);
	}

	public static Fixture makeBoxFixture(AgentBody agentBody, FixtureDef fdef, CFBitSeq categoryBits, CFBitSeq maskBits,
			Object userData, float width, float height) {
		return makeBoxFixture(agentBody, fdef, new AgentBodyFilter(categoryBits, maskBits, userData),
				width, height, new Vector2(0f, 0f));
	}

	public static Fixture makeSensorBoxFixture(AgentBody agentBody, CFBitSeq categoryBits, CFBitSeq maskBits,
			Object userData, float width, float height) {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		return makeBoxFixture(agentBody, fdef, new AgentBodyFilter(categoryBits, maskBits, userData),
				width, height, new Vector2(0f, 0f));
	}

	public static Fixture makeSensorBoxFixture(AgentBody agentBody, CFBitSeq categoryBits, CFBitSeq maskBits,
			Object userData, float width, float height, Vector2 position) {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		return makeBoxFixture(agentBody, fdef,
				new AgentBodyFilter(categoryBits, maskBits, userData), width, height, position);
	}

	public static Fixture makeBoxFixture(AgentBody agentBody, AgentBodyFilter abf, float width, float height) {
		return makeBoxFixture(agentBody, new FixtureDef(), abf, width, height, new Vector2(0f, 0f));
	}
}
