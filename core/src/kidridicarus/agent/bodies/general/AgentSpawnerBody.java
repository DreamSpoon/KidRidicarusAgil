package kidridicarus.agent.bodies.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.general.AgentSpawner;
import kidridicarus.info.GameInfo;

public class AgentSpawnerBody extends AgentBody implements Disposable {
	private AgentSpawner parent;

	public AgentSpawnerBody(AgentSpawner parent, World world, Rectangle bounds) {
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
		fdef.filter.categoryBits = GameInfo.SPAWNBOX_BIT;
		fdef.filter.maskBits = GameInfo.SPAWNTRIGGER_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, bounds.width, bounds.height);
	}

	@Override
	public AgentSpawner getParent() {
		return parent;
	}
}