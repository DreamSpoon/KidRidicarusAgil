package kidridicarus.game.agent.Metroid.NPC.skreeshot;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SkreeShotBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(6);
	private static final float BODY_HEIGHT = UInfo.P2M(6);

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.ROOM_BIT);

	private BasicAgentSpine spine;

	public SkreeShotBody(SkreeShot parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(0f);

		spine = new BasicAgentSpine(this);
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				getBodySize().x, getBodySize().y);
	}

	public BasicAgentSpine getSpine() {
		return spine;
	}
}
