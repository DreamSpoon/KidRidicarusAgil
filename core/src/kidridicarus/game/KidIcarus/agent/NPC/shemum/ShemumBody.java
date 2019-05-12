package kidridicarus.game.KidIcarus.agent.NPC.shemum;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

class ShemumBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(6f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = BODY_WIDTH;
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);
	private static final float PLAYER_SENSOR_WIDTH = UInfo.P2M(256);
	private static final float PLAYER_SENSOR_HEIGHT = UInfo.P2M(256);
	private static final Vector2 PLAYER_SENSOR_OFFSET = UInfo.VectorP2M(0f, -80);
	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.ROOM_BIT);

	private SolidContactSensor solidSensor;
	private AgentContactHoldSensor agentSensor;
	private AgentContactHoldSensor playerSensor;

	ShemumBody(Shemum parent, World world, Vector2 position, Vector2 velocity, SolidContactSensor solidSensor,
			AgentContactHoldSensor agentSensor, AgentContactHoldSensor playerSensor) {
		super(parent, world);
		this.solidSensor = solidSensor;
		this.agentSensor = agentSensor;
		this.playerSensor = playerSensor;
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		// main body fixture
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, solidSensor, getBounds().width,
				getBounds().height);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, agentSensor, getBounds().width,
				getBounds().height);
		// ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, solidSensor,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBounds().height/2f));
		// create player sensor fixture that covers most of the screen and detects players to target
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK,
				playerSensor, PLAYER_SENSOR_WIDTH, PLAYER_SENSOR_HEIGHT, PLAYER_SENSOR_OFFSET);
	}
}
