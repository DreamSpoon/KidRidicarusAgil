package kidridicarus.game.agent.SMB.player.mario;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.agent.SMB.BasicWalkAgent;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;

public class MarioFireball extends BasicWalkAgent implements DisposableAgent {
	private static final Vector2 MOVE_VEL = new Vector2(2.4f, -1.25f);
	private static final float MAX_Y_VEL = 2.0f;

	private Mario parent;

	private MarioFireballBody fbBody;
	private MarioFireballSprite fireballSprite;

	public enum MoveState { FLY, EXPLODE }
	private MoveState curMoveState;
	private float stateTimer;
	private boolean isMovingRight;	

	private enum ContactState { NONE, WALL, AGENT }
	private ContactState contactState;

	public MarioFireball(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		parent = properties.get(AgencyKV.Spawn.KEY_START_PARENTAGENT, null, Mario.class);

		contactState = ContactState.NONE;
		curMoveState = MoveState.FLY;
		stateTimer = 0f;

		// fireball on right?
		if(properties.containsKV(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT)) {
			isMovingRight = true;
			fbBody = new MarioFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MOVE_VEL.cpy().scl(1, 1));
		}
		// fireball on left
		else {
			isMovingRight = false;
			fbBody = new MarioFireballBody(this, agency.getWorld(), Agent.getStartPoint(properties),
					MOVE_VEL.cpy().scl(-1, 1));
		}

		fireballSprite = new MarioFireballSprite(agency.getAtlas(), fbBody.getPosition());

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private MoveState getNextMoveState() {
		if(contactState == ContactState.NONE)
			return MoveState.FLY;
		return MoveState.EXPLODE;
	}

	public void doUpdate(float delta) {
		processContacts();
		processMove(delta);
		processSprite(delta);
	}

	private void processContacts() {
		// if hit a wall or bounced off of something...
		if(fbBody.isMoveBlocked(isMovingRight) || (fbBody.getVelocity().x <= 0f && isMovingRight) ||
				(fbBody.getVelocity().x >= 0f && !isMovingRight)) {
			contactState = ContactState.WALL;
			return;
		}

		// check for agents needing damage, and damage the first one
		for(ContactDmgTakeAgent agent : fbBody.getContactAgentsByClass(ContactDmgTakeAgent.class)) {
			if(agent == parent)
				continue;
			agent.onDamage(parent, 1f, fbBody.getPosition());
			// at least one agent contact
			contactState = ContactState.AGENT;
			break;
		}
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case EXPLODE:
				if(nextMoveState != curMoveState) {
					fbBody.disableAllContacts();
					fbBody.setVelocity(0f, 0f);
					fbBody.setGravityScale(0f);
					if(contactState == ContactState.AGENT)
						agency.playSound(AudioInfo.Sound.SMB.KICK);
					else
						agency.playSound(AudioInfo.Sound.SMB.BUMP);
				}
				if(fireballSprite.isExplodeFinished())
					agency.disposeAgent(this);
				break;
			case FLY:
				break;
		}

		if(fbBody.getVelocity().y > MAX_Y_VEL)
			fbBody.setVelocity(fbBody.getVelocity().x, MAX_Y_VEL);
		else if(fbBody.getVelocity().y < -MAX_Y_VEL)
			fbBody.setVelocity(fbBody.getVelocity().x, -MAX_Y_VEL);

		// increment state timer if state stayed the same, otherwise reset timer
		stateTimer = nextMoveState == curMoveState ? stateTimer+delta : 0f;
		curMoveState = nextMoveState;
	}

	private void processSprite(float delta) {
		// update sprite position and graphic
		fireballSprite.update(delta, fbBody.getPosition(), curMoveState);
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(fireballSprite);
	}

	@Override
	public Vector2 getPosition() {
		return fbBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return fbBody.getBounds();
	}

	@Override
	public void disposeAgent() {
		fbBody.dispose();
	}

	public static ObjectProperties makeAP(Vector2 position, boolean right, Mario parentAgent) {
		ObjectProperties props = Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_MARIOFIREBALL, position);
		props.put(AgencyKV.Spawn.KEY_START_PARENTAGENT, parentAgent);
		if(right)
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_RIGHT);
		else
			props.put(CommonKV.KEY_DIRECTION, CommonKV.VAL_LEFT);
		return props;
	}
}