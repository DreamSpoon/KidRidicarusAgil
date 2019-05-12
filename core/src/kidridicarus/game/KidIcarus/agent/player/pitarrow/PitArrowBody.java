package kidridicarus.game.KidIcarus.agent.player.pitarrow;

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
import kidridicarus.common.tool.Direction4;

class PitArrowBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(3);
	private static final float BODY_HEIGHT = UInfo.P2M(3);
	private static final float GRAVITY_SCALE = 0f;
	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.KEEP_ALIVE_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.ROOM_BIT);

	private Direction4 arrowDir;
	private SolidContactSensor solidSensor;
	private AgentContactHoldSensor agentSensor;

	PitArrowBody(PitArrow parent, World world, Vector2 position, Vector2 velocity, Direction4 arrowDir,
			SolidContactSensor solidSensor, AgentContactHoldSensor agentSensor) {
		super(parent, world);
		this.solidSensor = solidSensor;
		this.agentSensor = agentSensor;
		this.arrowDir = arrowDir;
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		if(arrowDir.isHorizontal())
			setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		// if vertical then rotate body size by 90 degrees
		else
			setBoundsSize(BODY_HEIGHT, BODY_WIDTH);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		b2body.setBullet(true);
		// create main fixture
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, solidSensor, getBounds().width,
				getBounds().height);
		// create agent contact sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, agentSensor, getBounds().width,
				getBounds().height);
	}
}
