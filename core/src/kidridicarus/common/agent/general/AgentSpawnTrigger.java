package kidridicarus.common.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agentbody.general.AgentSpawnTriggerBody;

public class AgentSpawnTrigger extends Agent {
	private AgentSpawnTriggerBody stBody;
	private boolean enabled;

	public AgentSpawnTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// begin in the disabled state (will not trigger spawners)
		enabled = false;
		stBody = new AgentSpawnTriggerBody(this, agency.getWorld(), Agent.getStartBounds(properties));

		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		if(enabled)
			updateSpawnBoxes(delta);
	}

	private void updateSpawnBoxes(float delta) {
		for(Agent sb : stBody.getSpawnerContacts())
			sb.update(delta);
	}

	/*
	 * Set the target center position of the spawn trigger, and the trigger will move on update (mouse joint).
	 */
	public void setTarget(Vector2 position) {
		stBody.setPosition(position);
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return stBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return stBody.getBounds();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getEnabled() {
		return enabled;
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		stBody.dispose();
	}

	public static ObjectProperties makeAP(Vector2 position, float width, float height) {
		return Agent.createRectangleAP(AgencyKV.Spawn.VAL_AGENTSPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
