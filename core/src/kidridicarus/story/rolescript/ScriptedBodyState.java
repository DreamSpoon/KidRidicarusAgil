package kidridicarus.story.rolescript;

import com.badlogic.gdx.math.Vector2;

public class ScriptedBodyState {
	public boolean contactEnabled;
	public float gravityFactor;
	public Vector2 position;
	public Vector2 velocity;

	public ScriptedBodyState() {
		contactEnabled = false;	// default no contact
		gravityFactor = 0f;	// default zero gravity
		position = new Vector2(0f, 0f);
		velocity = new Vector2(0f, 0f);
	}

	public ScriptedBodyState(boolean contactEnabled, float gravityFactor, Vector2 position, Vector2 velocity) {
		this.contactEnabled = contactEnabled;
		this.gravityFactor = gravityFactor;
		this.position = position.cpy();
		this.velocity = velocity.cpy();
	}

	public ScriptedBodyState cpy() {
		return new ScriptedBodyState(this.contactEnabled, this.gravityFactor, this.position, this.velocity);
	}
}
