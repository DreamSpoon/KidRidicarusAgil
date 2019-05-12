package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

class AgentSpawnerBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT,
			CommonCF.Alias.SOLID_MAP_BIT);
	private static final float GRAVITY_SCALE = 0f;

	private AgentContactHoldSensor agentSensor;

	AgentSpawnerBody(AgentSpawner parent, World world, Rectangle bounds) {
		super(parent, world);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(GRAVITY_SCALE);
		agentSensor = new AgentContactHoldSensor(parent);
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, agentSensor, bounds.width, bounds.height);
	}

	<T> T getFirstContactByClass(Class<T> cls) {
		return agentSensor.getFirstContactByClass(cls);
	}
}
