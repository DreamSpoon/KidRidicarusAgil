package kidridicarus.game.SMB.agent.other;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agentsprite.other.FloatingPointsSprite;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.SMBInfo.PointAmount;

/*
 * SMB floating points, and 1-up.
 * 
 * Relative vs Absolute Points Notes:
 *   This distinction is necessary due to the way mario can gain points.
 * i.e. The sliding turtle shell points multiplier, and the consecutive head bounce multiplier.
 * 
 * The sliding turtle shell awards only absolute points, and head bounces award only relative points.
 * Currently, mario fireball strikes award only absolute points.
 */
public class FloatingPoints extends Agent {
	private static final float FLOAT_TIME = 1f;
	private static final float FLOAT_HEIGHT = UInfo.P2M(48);

	private FloatingPointsSprite pointsSprite;
	private float stateTimer;
	private Vector2 originalPosition;

	public FloatingPoints(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		originalPosition = Agent.getStartPoint(properties);

		// default to zero points
		PointAmount amount = PointAmount.ZERO;
		// check for point amount property
		if(properties.containsKey(GameKV.SMB.KEY_POINTAMOUNT))
			amount = properties.get(GameKV.SMB.KEY_POINTAMOUNT, null, PointAmount.class);

		Agent ud = properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Agent.class);
		// give points to player and get the actual amount awarded (since player may have points multiplier)
		if(ud != null && ud instanceof Mario) {
			// relative points can stack, absolute points can not
			amount = ((Mario) ud).givePoints(amount, properties.containsKV(GameKV.SMB.KEY_RELPOINTAMOUNT,
					AgencyKV.VAL_TRUE));
			if(amount == PointAmount.P1UP)
				agency.playSound(AudioInfo.Sound.SMB.UP1);
		}

		pointsSprite = new FloatingPointsSprite(agency.getAtlas(), originalPosition, amount);

		stateTimer = 0f;
		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_TOP);
	}

	@Override
	public void update(float delta) {
		float yOffset = stateTimer <= FLOAT_TIME ? FLOAT_HEIGHT * stateTimer / FLOAT_TIME : FLOAT_HEIGHT;
		pointsSprite.update(originalPosition.cpy().add(0f, yOffset));
		stateTimer += delta;
		if(stateTimer > FLOAT_TIME)
			agency.disposeAgent(this);
	}

	@Override
	public void draw(Batch batch){
		pointsSprite.draw(batch);
	}

	@Override
	public Vector2 getPosition() {
		return originalPosition;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(originalPosition.x, originalPosition.y, 0f, 0f);
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
	}

	public static ObjectProperties makeAP(PointAmount amt, boolean relative, Vector2 position,
			float yOffset, Agent parentAgent) {
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.VAL_FLOATINGPOINTS, position.cpy().add(0f, yOffset));
		props.put(GameKV.SMB.KEY_POINTAMOUNT, amt);
		if(relative)
			props.put(GameKV.SMB.KEY_RELPOINTAMOUNT, AgencyKV.VAL_TRUE);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		return props;
	}
}
