package kidridicarus.game.KidIcarus.agent.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.KidIcarus.KidIcarusKV;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeartBrain.AngelHeartSize;

public class AngelHeart extends CorpusAgent {
	private BasicAgentSpine spine;
	private AngelHeartBrain brain;
	private AngelHeartSprite sprite;

	public AngelHeart(AgentHooks agentHooks, ObjectProperties agentProps) {
		super(agentHooks, agentProps);
		spine = new BasicAgentSpine(this);
		body = new AngelHeartBody(this, agentHooks.physHooks, AP_Tool.getCenter(agentProps),
				spine.createAgentSensor());
		spine.setBody(body);
		brain = new AngelHeartBrain(agentHooks, spine, agentProps.get(KidIcarusKV.KEY_HEART_COUNT, 1, Integer.class));
		sprite = new AngelHeartSprite(agentHooks.atlas, AP_Tool.getCenter(agentProps), brain.getHeartSize());
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
		agentHooks.addDrawListener(CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	public static ObjectProperties makeAP(Vector2 position, int heartCount) {
		if(!AngelHeartSize.isValidHeartCount(heartCount))
			throw new IllegalArgumentException("Unable to create Agent with heart count = " + heartCount);
		ObjectProperties props = AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_ANGEL_HEART, position);
		props.put(KidIcarusKV.KEY_HEART_COUNT, heartCount);
		return props;
	}
}
