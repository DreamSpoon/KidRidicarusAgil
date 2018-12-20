package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SimpleWalkAgent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.bodies.SMB.item.PowerStarBody;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ItemAgent;
import kidridicarus.agent.sprites.SMB.item.PowerStarSprite;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PowerupType;
import kidridicarus.info.UInfo;

/*
 * TODO:
 * -allow the star to spawn down-right out of bricks like on level 1-1
 * -test the star's onBump method - I could not bump it, needs precise timing - maybe loosen the timing? 
 */
public class PowerStar extends SimpleWalkAgent implements ItemAgent, BumpableAgent {
	private static final float SPROUT_TIME = 0.5f;
	private static final Vector2 START_BOUNCE_VEL = new Vector2(0.5f, 2f); 
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);
	private enum StarState { SPROUT, WALK };

	private PowerStarBody starBody;
	private PowerStarSprite starSprite;
	private boolean isSprouting;
	private Vector2 sproutingPosition;

	private float stateTimer;
	private StarState prevState;

	public PowerStar(Agency agency, AgentDef adef) {
		super(agency, adef);

		// start in the SPROUT state
		isSprouting = true;
		setConstVelocity(START_BOUNCE_VEL);
		starBody = null;
		sproutingPosition = adef.bounds.getCenter(new Vector2());
		starSprite = new PowerStarSprite(agency.getEncapTexAtlas(), sproutingPosition.cpy().add(0f, SPROUT_OFFSET));

		prevState = StarState.SPROUT;
		stateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	private StarState getState() {
		// still sprouting?
		if(isSprouting)
			return StarState.SPROUT;
		else
			return StarState.WALK;
	}

	@Override
	public void update(float delta) {
		float yOffset = 0f;
		StarState curState = getState();
		switch(curState) {
			case WALK:
				// start bounce to the right if this is first time walking
				if(prevState == StarState.SPROUT) {
					starBody.applyImpulse(START_BOUNCE_VEL);
					break;
				}

				// clamp y velocity and maintain steady x velocity
				if(starBody.getVelocity().y > getConstVelocity().y)
					starBody.setVelocity(getConstVelocity().x, getConstVelocity().y);
				else if(starBody.getVelocity().y < -getConstVelocity().y)
					starBody.setVelocity(getConstVelocity().x, -getConstVelocity().y);
				else
					starBody.setVelocity(getConstVelocity().x, starBody.getVelocity().y);
				break;
			case SPROUT:
				if(stateTimer > SPROUT_TIME) {
					isSprouting = false;
					agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
					starBody = new PowerStarBody(this, agency.getWorld(), sproutingPosition);
				}
				else
					yOffset = SPROUT_OFFSET * (SPROUT_TIME - stateTimer) / SPROUT_TIME;
				break;
		}

		if(starBody != null)
			starSprite.update(delta, starBody.getPosition().cpy().add(0f, yOffset));
		else
			starSprite.update(delta, sproutingPosition.cpy().add(0f, yOffset));

		// increment state timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;
	}

	@Override
	public void draw(Batch batch){
		starSprite.draw(batch);
	}

	@Override
	public void use(Agent agent) {
		if(stateTimer <= SPROUT_TIME)
			return;

		if(agent instanceof Mario) {
			((Mario) agent).applyPowerup(PowerupType.POWERSTAR);
			agency.disposeAgent(this);
		}
	}

	public void onContactBoundLine(LineSeg seg) {
		// bounce off of vertical bounds only
		if(!seg.isHorizontal)
			reverseConstVelocity(true,  false);
	}

	@Override
	public Vector2 getPosition() {
		return starBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return starBody.getBounds();
	}

	@Override
	public void onBump(Agent perp, Vector2 fromCenter) {
		if(stateTimer <= SPROUT_TIME)
			return;

		// if bump came from left and star is moving left then reverse,
		// if bump came from right and star is moving right then reverse
		if((fromCenter.x < starBody.getPosition().x && starBody.getVelocity().x < 0f) ||
			(fromCenter.x > starBody.getPosition().x && starBody.getVelocity().x > 0f))
			reverseConstVelocity(true, false);

		starBody.setVelocity(getConstVelocity().x, getConstVelocity().y);
	}

	@Override
	public void dispose() {
		starBody.dispose();
	}
}
