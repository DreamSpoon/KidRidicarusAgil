package kidridicarus.agent.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SimpleWalkAgent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.bodies.SMB.enemy.TurtleBody;
import kidridicarus.agent.optional.AgentContactAgent;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.optional.DamageableAgent;
import kidridicarus.agent.optional.HeadBounceAgent;
import kidridicarus.agent.sprites.SMB.enemy.TurtleSprite;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;

/*
 * TODO:
 * -Do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 * -turtle shells do not slide properly when they are kicked while contacting an agent, since the slide kill
 *  agent code is only called when contacting starts
 */
public class Turtle extends SimpleWalkAgent implements HeadBounceAgent, ContactDmgAgent, BumpableAgent,
		DamageableAgent, AgentContactAgent {
	private static final float WALK_VEL = 0.4f;
	private static final float BUMP_UP_VEL = 2f;
	private static final float BUMP_SIDE_VEL = 1f;
	private static final float SLIDE_VEL = 2f;
	private static final float WAKING_TIME = 3f;
	private static final float WAKE_UP_DELAY = 1.7f;
	private static final float DIE_FALL_TIME = 6f;

	public enum TurtleState { WALK, HIDE, WAKE_UP, SLIDE, DEAD };

	private TurtleBody turtleBody;
	private TurtleSprite turtleSprite;

	private TurtleState prevState;
	private float stateTimer;

	private boolean facingRight;
	private boolean isHiding;	// after mario bounces on head, turtle hides in shell
	private boolean isWaking;
	private boolean isSliding;
	private boolean isDead;
	private boolean isDeadToRight;
	private boolean isHeadBounced;
	private Agent perp;
	private PointAmount slidingTotal;

	public Turtle(Agency agency, AgentDef adef) {
		super(agency, adef);

		turtleBody = new TurtleBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		turtleSprite = new TurtleSprite(agency.getEncapTexAtlas(), adef.bounds.getCenter(new Vector2()));

		setConstVelocity(-WALK_VEL, 0f);
		facingRight = false;
		isHiding = false;
		isWaking = false;
		isSliding = false;
		isDead = false;
		isDeadToRight = false;
		isHeadBounced = false;
		perp = null;
		// the more sequential hits while sliding the higher the points per hit
		slidingTotal = PointAmount.ZERO;

		prevState = TurtleState.WALK;
		stateTimer = 0f;

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
	}

	public void update(float delta) {
		TurtleState curState = getState();
		switch(curState) {
			case DEAD:
				// newly deceased?
				if(curState != prevState)
					startDeath();
				// check the old deceased for timeout
				else if(stateTimer > DIE_FALL_TIME)
					agency.disposeAgent(this);
				break;
			case HIDE:
				// wait a short time and reappear
				if(curState != prevState) {
					isWaking = false;
					startHideInShell();
				}
				else if(stateTimer > WAKE_UP_DELAY)
					isWaking = true;
				break;
			case WAKE_UP:
				if(curState == prevState && stateTimer > WAKING_TIME)
					endHideInShell();
				break;
			case SLIDE:
				if(curState != prevState)
					startSlide();
				// Intentionally not using break;
				// Because sliding turtle needs to move when onGround.
			case WALK:
				if(turtleBody.isOnGround())
					turtleBody.setVelocity(getConstVelocity());
				break;
		}

		// update sprite position and graphic
		turtleSprite.update(delta, turtleBody.getPosition(), curState, facingRight);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;

		isHeadBounced = false;
	}

	private TurtleState getState() {
		if(isDead)
			return TurtleState.DEAD;
		else if(isSliding)
			return TurtleState.SLIDE;
		else if(isHiding) {
			if(isWaking)
				return TurtleState.WAKE_UP;
			else
				return TurtleState.HIDE;
		}
		else
			return TurtleState.WALK;
	}

	private void startSlide() {
		agency.playSound(AudioInfo.SOUND_KICK);
		slidingTotal = PointAmount.P400;
		if(perp != null) {
			agency.createAgent(ADefFactory.makeFloatingPointsDef(slidingTotal, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), (Mario) perp));
		}
	}

	private void startHideInShell() {
		// stop moving
		turtleBody.zeroVelocity();
		agency.playSound(AudioInfo.SOUND_STOMP);
		if(perp != null) {
			agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P100, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), (Mario) perp));
		}
	}

	private void endHideInShell() {
		isWaking = false;
		isHiding = false;
		if(turtleBody.isOnGround())
			turtleBody.setVelocity(getConstVelocity());
	}

	private void startDeath() {
		turtleBody.disableContacts();
		turtleBody.zeroVelocity();

		// die move to the right or die move to to the left?
		if(isDeadToRight)
			turtleBody.applyImpulse(new Vector2(BUMP_SIDE_VEL, BUMP_UP_VEL));
		else
			turtleBody.applyImpulse(new Vector2(-BUMP_SIDE_VEL, BUMP_UP_VEL));

		if(perp != null) {
			agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P500, isHeadBounced,
					turtleBody.getPosition(), UInfo.P2M(16), (Mario) perp));
		}
	}

	@Override
	public void draw(Batch batch){
		turtleSprite.draw(batch);
	}

	@Override
	public void onHeadBounce(Agent perp, Vector2 fromPos) {
		if(isDead)
			return;

		isHeadBounced = true;
		this.perp = perp;
		if(isSliding)
			cancelSlide();
		else if(isHiding) {
			if(fromPos.x > turtleBody.getPosition().x)
				initSlide(false);	// slide right
			else
				initSlide(true);	// slide left
		}
		else
			isHiding = true;
	}

	private void initSlide(boolean right) {
		isSliding = true;
		facingRight = right;
		if(right)
			setConstVelocity(SLIDE_VEL, 0f);
		else
			setConstVelocity(-SLIDE_VEL, 0f);
	}

	private void cancelSlide() {
		isSliding = false;
		if(getConstVelocity().x > 0)
			setConstVelocity(WALK_VEL, 0f);
		else
			setConstVelocity(-WALK_VEL, 0f);
	}

	@Override
	public void onContactAgent(Agent agent) {
		// if walking normally and contacted another agent then reverse direction
		if(!isHiding && !isSliding) {
			reverseConstVelocity(true, false);
			facingRight = !facingRight;
		}
		else if(isSliding) {
			// if hit another sliding turtle, then both die
			if(agent instanceof Turtle && ((Turtle) agent).isSliding) {
				((DamageableAgent) agent).onDamage(perp, 1f, turtleBody.getPosition());
				onDamage(perp, 1f, agent.getPosition());
			}
			// else if sliding and strikes a dmgable bot...
			else if(agent instanceof DamageableAgent) {
				// give the dmgable bot a null player ref so that it doesn't give points, we will give points here
				((DamageableAgent) agent).onDamage(null, 1f, agent.getPosition());
				agency.playSound(AudioInfo.SOUND_KICK);
				if(perp != null) {
					slidingTotal = slidingTotal.increment();
					agency.createAgent(ADefFactory.makeFloatingPointsDef(slidingTotal, isHeadBounced,
							turtleBody.getPosition(), UInfo.P2M(16), (Mario) perp));
				}
			}
		}
	}

	public void onContactBoundLine(LineSeg seg) {
		// bounce off of vertical bounds
		if(!seg.isHorizontal) {
			reverseConstVelocity(true, false);
			facingRight = !facingRight;
			if(isSliding)
				agency.playSound(AudioInfo.SOUND_BUMP);
		}
	}

	// assume any amount of damage kills, for now...
	@Override
	public void onDamage(Agent perp, float amount, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < turtleBody.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	@Override
	public void onBump(Agent perp, Vector2 fromCenter) {
		this.perp = perp;
		isDead = true;
		if(fromCenter.x < turtleBody.getPosition().x)
			isDeadToRight = true;
		else
			isDeadToRight = false;
	}

	 // the player can "kick" a turtle hiding in its shell
	public void onGuideContact(Agent perp, Vector2 position) {
		if(isDead)
			return;

		// a living turtle hiding in the shell and not sliding can be "pushed" to slide
		if(isHiding && !isSliding) {
			this.perp = perp;
			// pushed from left?
			if(position.x < turtleBody.getPosition().x)
				initSlide(true);	// slide right
			else
				initSlide(false);	// slide left
		}
	}

	@Override
	public boolean isContactDamage() {
		if(isDead || (isHiding && !isSliding))
			return false;
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return turtleBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return turtleBody.getBounds();
	}

	@Override
	public void dispose() {
		turtleBody.dispose();
	}
}
