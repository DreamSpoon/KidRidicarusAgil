package kidridicarus.game.SMB.agentbody.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.info.CommonCF;
import kidridicarus.game.SMB.agent.other.BumpTile;
import kidridicarus.game.SMB.agentbody.BumpableTileBody;

public class BumpTileBody extends AgentBody implements BumpableTileBody {
	private BumpTile parent;

	public BumpTileBody(World world, BumpTile parent, Rectangle bounds) {
		this.parent = parent;
		setBodySize(bounds.width, bounds.height);

		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.StaticBody;
		bdef.position.set(bounds.getCenter(new Vector2()));
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, CommonCF.BUMPTILE_CFCAT,
				CommonCF.BUMPTILE_CFMASK, bounds.width, bounds.height);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	@Override
	public void onBumpTile(Agent bumpingAgent) {
		parent.onBumpTile(bumpingAgent);
	}
}