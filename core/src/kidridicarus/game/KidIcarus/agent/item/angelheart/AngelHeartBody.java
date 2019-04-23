package kidridicarus.game.KidIcarus.agent.item.angelheart;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agent.staticpowerup.StaticPowerupBody;
import kidridicarus.common.info.UInfo;

public class AngelHeartBody extends StaticPowerupBody {
	private static final float BODY_WIDTH = UInfo.P2M(3f);
	private static final float BODY_HEIGHT = UInfo.P2M(3f);

	public AngelHeartBody(AngelHeart parent, World world, Vector2 position) {
		super(parent, world, new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT));
	}
}