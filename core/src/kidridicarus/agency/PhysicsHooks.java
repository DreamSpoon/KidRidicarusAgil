package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class PhysicsHooks {
	private final Agency myAgency;

	PhysicsHooks(Agency agency) {
		this.myAgency = agency;
	}

	public Body createBody(BodyDef bdef) {
		return myAgency.panWorld.createBody(bdef);
	}

	public void destroyBody(Body body) {
		myAgency.panWorld.destroyBody(body);
	}

	public Joint createJoint(MouseJointDef mjdef) {
		return myAgency.panWorld.createJoint(mjdef);
	}
}
