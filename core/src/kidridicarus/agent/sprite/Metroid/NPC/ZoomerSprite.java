package kidridicarus.agent.sprite.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.GameInfo.Direction4;
import kidridicarus.agent.Metroid.NPC.Zoomer.MoveState;
import kidridicarus.info.MetroidAnim;
import kidridicarus.info.UInfo;

public class ZoomerSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.05f;

	private Animation<TextureRegion> walkAnim;
	private float stateTimer;

	public ZoomerSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.NPC.ZOOMER), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	// TODO: change angle of sprite based on upDir
	public void update(float delta, Vector2 position, MoveState curState, Direction4 upDir) {
		switch(curState) {
			case WALK:
				setRegion(walkAnim.getKeyFrame(stateTimer, true));
				break;
			case DEAD:
				break;
		}

		stateTimer += delta;
		switch(upDir) {
			case RIGHT:
				setRotation(270);
				break;
			case UP:
				setRotation(0);
				break;
			case LEFT:
				setRotation(90);
				break;
			case DOWN:
				setRotation(180);
				break;
		}
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}