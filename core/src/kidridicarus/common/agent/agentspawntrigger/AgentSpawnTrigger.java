package kidridicarus.common.agent.agentspawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;

public class AgentSpawnTrigger extends FollowBox implements DisposableAgent {
	private AgentSpawnTriggerBody body;
	private boolean enabled;

	public AgentSpawnTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		// begin in the disabled state (will not trigger spawners)
		enabled = false;
		body = new AgentSpawnTriggerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(); }
			});
	}

	private void doUpdate() {
		if(!enabled)
			return;
		for(EnableTakeAgent agent : body.getAndResetBeginContacts())
			agent.onTakeEnable(true);
		for(EnableTakeAgent agent : body.getAndResetEndContacts())
			agent.onTakeEnable(false);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getEnabled() {
		return enabled;
	}

	@Override
	protected FollowBoxBody getFollowBoxBody() {
		return body;
	}

	public static ObjectProperties makeAP(Vector2 position, float width, float height) {
		return Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_AGENTSPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
