package kidridicarus.game.agent.Metroid.item.energy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class EnergyBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(4f);
	private static final float BODY_HEIGHT = UInfo.P2M(4f);

	private BasicAgentSpine spine;

	public EnergyBody(Energy parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(new Rectangle(position.x, position.y, 0f, 0f));
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, bounds.getCenter(new Vector2()));
		createFixtures();
	}

	private void createBody(World world, Vector2 position) {
		b2body = B2DFactory.makeDynamicBody(world, position);
		b2body.setGravityScale(0f);

		spine = new BasicAgentSpine(this);
	}

	private void createFixtures() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, getBodySize().x, getBodySize().y);
	}

	public BasicAgentSpine getSpine() {
		return spine;
	}
}
