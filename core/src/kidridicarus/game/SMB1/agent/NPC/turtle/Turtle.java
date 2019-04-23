package kidridicarus.game.SMB1.agent.NPC.turtle;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.SMB1.agent.Koopa;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.SMB1_Audio;

/*
 * TODO:
 * -do sliding turtle shells break bricks when they strike them?
 *  I couldn't find any maps in SMB 1 that would clear up this matter.
 */
public class Turtle extends PlacedBoundsAgent implements Koopa, ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
	private static final float GIVE_DAMAGE = 8f;
	private static final float DIE_FALL_TIME = 6f;
	private static final float HIDE_DELAY = 1.7f;
	private static final float WAKE_DELAY = 3f;

	enum MoveState { WALK, FALL, DEAD, HIDE, WAKE, SLIDE;
			public boolean equalsAny(MoveState ...otherStates) {
				for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
			}
			public boolean isKickable() { return this.equalsAny(HIDE, WAKE); }
		}

	private enum HitType { NONE, BOUNCE, KICK }

	private TurtleBody body;
	private TurtleSprite sprite;

	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private HitType currentHit;
	private boolean isDead;
	private boolean deadBumpRight;
	private Agent perp;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public Turtle(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		currentHit = HitType.NONE;
		isDead = false;
		deadBumpRight = false;
		perp = null;
		despawnMe = false;
		lastKnownRoom = null;

		body = new TurtleBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.POST_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		sprite = new TurtleSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
			});
	}

	private void doContactUpdate() {
		HitType hitCheck = HitType.NONE;
		for(Agent agent : body.getSpine().getHeadBounceBeginContacts()) {
			if(moveState == MoveState.SLIDE)
				hitCheck = slideContact(agent);
			else if(moveState.isKickable())
				hitCheck = kickableContact(agent);
			else
				hitCheck = walkContact(agent);

			if(hitCheck != HitType.NONE)
				break;
		}
		if(hitCheck != HitType.NONE)
			currentHit = hitCheck;
	}

	private HitType slideContact(Agent agent) {
		// if agent can give head bounces and head bounce is successful
		if(agent instanceof HeadBounceGiveAgent && ((HeadBounceGiveAgent) agent).onGiveHeadBounce(this)) {
			perp = agent;
			return HitType.BOUNCE;
		}
		// if agent can take damage then do it
		if(agent instanceof ContactDmgTakeAgent)
			((ContactDmgTakeAgent) agent).onTakeDamage(perp, GIVE_DAMAGE, body.getPosition());

		return HitType.NONE;
	}

	// returns true if kicked by agent
	private HitType kickableContact(Agent agent) {
		if(agent instanceof HeadBounceGiveAgent) {
			// try to pull head bounce
			if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(this)) {
				perp = agent;
				return HitType.BOUNCE;
			}
			else {
				perp = agent;
				return HitType.KICK;
			}
		}
		else if(agent instanceof PlayerAgent) {
			perp = agent;
			return HitType.KICK;
		}
		return HitType.NONE;
	}

	private HitType walkContact(Agent agent) {
		// if they take contact damage and give head bounces...
		if(agent instanceof ContactDmgTakeAgent && agent instanceof HeadBounceGiveAgent) {
			if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(this)) {
				perp = agent;
				return HitType.BOUNCE;
			}
			// if can't pull head bounce then try pushing contact damage
			else
				((ContactDmgTakeAgent) agent).onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
		}
		// pull head bounces from head bounce agents
		else if(agent instanceof HeadBounceGiveAgent) {
			if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(this)) {
				perp = agent;
				return HitType.BOUNCE;
			}
		}
		// push damage to contact damage agents
		else if(agent instanceof ContactDmgTakeAgent)
			((ContactDmgTakeAgent) agent).onTakeDamage(this, GIVE_DAMAGE, body.getPosition());

		return HitType.NONE;
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !body.getSpine().isTouchingKeepAlive()) || body.getSpine().isContactDespawn())
			despawnMe = true;
	}

	private void processMove(float delta) {
		if(despawnMe) {
			agency.removeAgent(this);
			return;
		}

		boolean isSliding = moveState == MoveState.SLIDE;
		if(body.getSpine().isKoopaSideMoveBlocked(isFacingRight, !isSliding)) {
			isFacingRight = !isFacingRight;
			if(isSliding)
				agency.getEar().playSound(SMB1_Audio.Sound.BUMP);
		}

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL:
				break;
			case HIDE:
				if(moveStateChanged) {
					agency.createAgent(FloatingPoints.makeAP(100, true, body.getPosition(), perp));
					body.zeroVelocity(true, true);
					agency.getEar().playSound(SMB1_Audio.Sound.STOMP);
				}
				break;
			case WAKE:
				break;
			case SLIDE:
				// if in first frame of slide then check if facing direction should change because of perp's position
				if(moveStateChanged) {
					if(isFacingRight && body.getSpine().isOtherAgentOnRight(perp))
						isFacingRight = false;
					else if(!isFacingRight && !body.getSpine().isOtherAgentOnRight(perp))
						isFacingRight = true;
					agency.getEar().playSound(SMB1_Audio.Sound.KICK);
					agency.createAgent(FloatingPoints.makeAP(400, true, body.getPosition(), perp));
				}
				body.getSpine().doSlideMove(isFacingRight);
				break;
			case DEAD:
				// newly deceased?
				if(moveStateChanged) {
					doStartDeath();
					agency.createAgent(FloatingPoints.makeAP(500, true, body.getPosition(), perp));
				}
				// check the old deceased for timeout
				else if(moveStateTimer > DIE_FALL_TIME)
					agency.removeAgent(this);
				break;
		}

		// reset this flag
		currentHit = HitType.NONE;

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		// increment state timer if state stayed the same, otherwise reset timer
		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(isDead)
			return MoveState.DEAD;
		else if(currentHit != HitType.NONE) {
			if(moveState.isKickable())
				return MoveState.SLIDE;
			else
				return MoveState.HIDE;
		}
		else if(moveState == MoveState.SLIDE)
			return MoveState.SLIDE;
		else if(moveState == MoveState.HIDE) {
			if(moveStateTimer > HIDE_DELAY)
				return MoveState.WAKE;
			else
				return MoveState.HIDE;
		}
		else if(moveState == MoveState.WAKE) {
			if(moveStateTimer > WAKE_DELAY) {
				if(body.getSpine().isOnGround())
					return MoveState.WALK;
				else
					return MoveState.FALL;
			}
			else
				return MoveState.WAKE;
		}
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void doStartDeath() {
		body.getSpine().doDeadBumpContactsAndMove(deadBumpRight);
		agency.createAgent(FloatingPoints.makeAP(500, false, body.getPosition(), perp));
	}

	private void doPostUpdate() {
		if(moveState != MoveState.DEAD) {
			RoomBox nextRoom = body.getSpine().getCurrentRoom();
			if(nextRoom != null)
				lastKnownRoom = nextRoom;
		}
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		sprite.update(delta, body.getPosition(), moveState, isFacingRight);
	}

	private void doDraw(Eye adBatch) {
		// if not despawning then draw
		if(!despawnMe)
			adBatch.draw(sprite);
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		this.perp = agent;
		isDead = true;
		deadBumpRight = !body.getSpine().isDeadBumpRight(dmgOrigin);
		return true;
	}

	@Override
	public void onTakeBump(Agent agent) {
		if(isDead)
			return;

		// save ref to perpetrator Agent, and check for bump right
		this.perp = agent;
		isDead = true;
		Vector2 perpPos = AP_Tool.getCenter(perp);
		if(perpPos == null)
			deadBumpRight = false;
		else
			deadBumpRight = body.getSpine().isDeadBumpRight(perpPos);
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}