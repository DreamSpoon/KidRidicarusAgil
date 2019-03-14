package kidridicarus.game.agent.SMB.other.levelendtrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.agent.SMB.other.castleflag.CastleFlag;
import kidridicarus.game.info.GameKV;

public class LevelEndTrigger extends Agent implements DisposableAgent {
	private LevelEndTriggerBody leBody;

	public LevelEndTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		leBody = new LevelEndTriggerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	public boolean use(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			return false;

		// trigger the castle flag
		triggerCastleFlag();
		// start player script with name of next level
		return ((PlayerAgent) agent).getSupervisor().startScript(new LevelEndScript(
				getProperty(CommonKV.Level.VAL_NEXTLEVEL_NAME, "", String.class)));
	}

	private void triggerCastleFlag() {
		Agent agent = agency.getFirstAgentByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS },
				new String[] { GameKV.SMB.AgentClassAlias.VAL_CASTLEFLAG });
		if(agent instanceof CastleFlag)
			((CastleFlag) agent).trigger();
	}

	@Override
	public Vector2 getPosition() {
		return leBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return leBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		leBody.dispose();
	}
}