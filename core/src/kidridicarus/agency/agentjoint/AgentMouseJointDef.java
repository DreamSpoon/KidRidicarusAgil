package kidridicarus.agency.agentjoint;

import com.badlogic.gdx.math.Vector2;

public class AgentMouseJointDef extends AgentJointDef {
	public final Vector2 target = new Vector2();
	public float maxForce = 0;
	public float frequencyHz = 5.0f;
	public float dampingRatio = 0.7f;

	public AgentMouseJointDef () {
		type = AgentJointType.MOUSE_JOINT;
	}
}
