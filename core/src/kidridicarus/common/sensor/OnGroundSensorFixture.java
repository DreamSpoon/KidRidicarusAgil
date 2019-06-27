package kidridicarus.common.sensor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.AgencyContactListener.PreSolver;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.agency.AgentBody;
import kidridicarus.agency.AgentContactHalf;
import kidridicarus.agency.AgentContactSensor;
import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.AgentFixtureDef;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.tiledmap.solidlayer.SolidLineSeg;

/*
 * Unfortunately, this fixture requires a "parent" fixture in order to operate. But, why would anyone need an
 * "on-ground" sensor unless there was a fixture that needed checking for on-ground state?
 */
public class OnGroundSensorFixture {
	private AgentContactHalf myContactHalf;
	private AgentFixture myFixture;
	private AgentContactSensor fullSolidSensor;
	private AgentContactSensor condSolidSensor;

	public OnGroundSensorFixture(AgentBody agentBody, AgentFixture parentFixture, Vector2 size,
			Vector2 position) {
		this.myContactHalf = new AgentContactHalf(agentBody.getAgent(), agentBody, parentFixture);
		// create the fixture and the sensors
		AgentFixtureDef afDef = new AgentFixtureDef();
		afDef.isSensor = true;
		afDef.agentFilter.set(CommonCF.HALF_SOLID_FILTER);
		afDef.shape = new PolygonShape();
		((PolygonShape) afDef.shape).setAsBox(size.x/2f, size.y/2f, position, 0f);
		this.myFixture = agentBody.createFixture(afDef);
		this.fullSolidSensor = this.myFixture.createSensor(CommonCF.HALF_SOLID_FILTER);
		this.condSolidSensor = this.myFixture.createSensor(new AgentFilter(
				new FilterBitSet(CommonCF.ACFB.COND_SOLID_ALWAYS_TAKEBIT),
				new FilterBitSet(CommonCF.ACFB.COND_SOLID_ALWAYS_GIVEBIT)));
	}

	public boolean isOnGround() {
		// check this fixture against full solids, returning true if a floor contact is found
		for(AgentFixture otherFixture : fullSolidSensor.getCurrentContacts()) {
			// is the contacted thing a solid line segment?
			Object contactedThing = otherFixture.getUserData();
			if(SolidLineSeg.class.isAssignableFrom(contactedThing.getClass())) {
				SolidLineSeg lineSeg = (SolidLineSeg) contactedThing;
				// if the solid line segment is a floor then return true
				if(lineSeg.isHorizontal && lineSeg.upNormal)
					return true;
			}
		}
		// conditional solids are checked against the parent fixture and, if solid, then true is returned
		for(AgentFixture otherFixture : condSolidSensor.getCurrentContacts()) {
			Object contactedThing = otherFixture.getUserData();
			if(PreSolver.class.isAssignableFrom(contactedThing.getClass())) {
				PreSolver otherPreSolver = (PreSolver) contactedThing;
				// if the pre-solver would return true against the sensor's parent fixture then return true
				if(otherPreSolver.preSolve(myContactHalf,
						new AgentContactHalf(otherFixture.getAgent(), otherFixture.getAgentBody(), otherFixture))) {
					return true;
				}
			}
		}
		return false;
	}
}
