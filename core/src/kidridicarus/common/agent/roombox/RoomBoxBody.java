package kidridicarus.common.agent.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class RoomBoxBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.ROOM_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	public RoomBoxBody(RoomBox parent, World world, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		// TODO change to makeSensorBoxFixture?
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, this, bounds.width, bounds.height);
	}
}
