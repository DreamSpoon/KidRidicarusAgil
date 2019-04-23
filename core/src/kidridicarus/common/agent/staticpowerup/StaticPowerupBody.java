package kidridicarus.common.agent.staticpowerup;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public abstract class StaticPowerupBody extends AgentBody {
	private BasicAgentSpine spine;

	public StaticPowerupBody(StaticPowerup parent, World world, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// define new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(0f);
		spine = new BasicAgentSpine(this);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK,
				spine.createAgentSensor(), getBounds().width, getBounds().height);
	}

	public BasicAgentSpine getSpine() {
		return spine;
	}
}