package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;

public class PitArrow extends CorpusAgent {
	private SolidContactSpine spine;
	private PitArrowBrain brain;
	private PitArrowSprite sprite;

	public PitArrow(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		Direction4 arrowDir = properties.getDirection4(CommonKV.KEY_DIRECTION, Direction4.NONE);
		spine = new SolidContactSpine(this);
		body = new PitArrowBody(this, agentHooks.physHooks, AP_Tool.getCenter(properties),
				AP_Tool.safeGetVelocity(properties), arrowDir, spine.createSolidContactSensor(),
				spine.createAgentSensor());
		spine.setBody(body);
		brain = new PitArrowBrain(properties.get(CommonKV.KEY_PARENT_AGENT, null, Pit.class), agentHooks,
				spine, properties.getBoolean(CommonKV.Spawn.KEY_EXPIRE, false), arrowDir);
		sprite = new PitArrowSprite(agentHooks.atlas,
				new PitArrowSpriteFrameInput(body.getPosition(), arrowDir));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame();
				}
			});
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					sprite.processFrame(brain.processFrame(frameTime.timeDelta));
				}
			});
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Pit parentAgent, Vector2 position, Vector2 velocity, Direction4 arrowDir,
			boolean isExpireImmediately) {
		ObjectProperties props = AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW, position, velocity);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		props.put(CommonKV.KEY_DIRECTION, arrowDir);
		props.put(CommonKV.Spawn.KEY_EXPIRE, isExpireImmediately);
		return props;
	}
}
