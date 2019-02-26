package kidridicarus.agency.agent.body.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.AgentBody;
import kidridicarus.agency.agent.general.GuideSpawner;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.tool.B2DFactory;

public class GuideSpawnerBody extends AgentBody {
	private GuideSpawner parent;

	public GuideSpawnerBody(World world, GuideSpawner parent, Rectangle bounds) {
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
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, new CFBitSeq(), new CFBitSeq(),
				bounds.width, bounds.height);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}