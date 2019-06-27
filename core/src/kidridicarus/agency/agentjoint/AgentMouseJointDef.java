package kidridicarus.agency.agentjoint;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class AgentMouseJointDef extends AgentJointDef {
	public final Vector2 target;
	public float maxForce;
	public float frequencyHz;
	public float dampingRatio;

	public AgentMouseJointDef () {
		this.type = AgentJointType.MOUSE_JOINT;
		this.target = new Vector2();
		MouseJointDef defaultDef = new MouseJointDef();
		this.maxForce = defaultDef.maxForce;
		this.frequencyHz = defaultDef.frequencyHz;
		this.dampingRatio = defaultDef.dampingRatio;
	}
}
