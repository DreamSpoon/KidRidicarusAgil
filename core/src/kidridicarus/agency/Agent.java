package kidridicarus.agency;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.agency.tool.draodgraph.DepNode;

/*
 * Agents have a key-value list of properties that can be queried. The list can be queried for info such as current
 * position, facing direction, initial velocity, etc.
 * A userData variable is also provided, for uses similar to those of the Box2D userData variable.
 * The userData variable is read-only outside the Agent, and read/write inside the Agent.
 *
 * TODO
 * Agents will have observable 2x2 perspective: (inward, outward) x (audio, video)
 * where
 * Outward perspective:
 *   Sprite - e.g. Mario fireball sprite in explode animation state
 *   Speaker (sound) - e.g. Goomba playing it's head bopped sound
 * Inward perspective:
 *   Screen center
 *   Music being "heard" - e.g. Mario's Power star Music is heard more loudly than the room music
 *
 * Screen center can be used to determine which Agents will be "observed" for sight output (video, sprites, etc.),
 * and for speaker output (audio, music, etc.).
 * When a "draw" pass is run, it should be run in parts:
 *   1) Find the player with the "inward perspective" that you want to use.
 *   2) Get the screen center from the "inward perspective".
 *   3) Get the music / audio data from "inward perspective" and set music/audio accordingly.
 *   4) Get the sprite/speaker data from Agents within "sight"/"earshot" based on the "inward perspective".
 * How to divide up these parts between the caller and the Agent/Agency is a work in progress.
 *
 * Player Agents will probably have fields for all four classes (working names):
 *    class AgentSprite { ... }		// show visual
 *    class AgentSpeaker { ... }	// play sounds
 *    class AgentEye { ... }		// "see" screen center
 *    class AgentEar { ... }		// "hear" music changes
 *
 *
 * Perspective and Perception
 * I perceive the sprites on the screen through a visual perspective. Suppose Perspective is an object.
 * Aside: I also perceive the sound "on-screen", or "on-speaker", through a perspective.
 * The sprite is the thing that is being perceived, the perspective is the thing that is doing the perceiving.
 * The sprite is perceived by a perspective.
 * Sprite and perspective are nouns. Perceived is a verb.
 * "sprite" and "perspective" are class objects, and "perceived" is a method in a class.
 *
 * Nomenclature: pickup
 * See wikipedia article on Magnetic Cartridge as it relates to vinyl turntable record playback.
 *     https://en.wikipedia.org/wiki/Magnetic_cartridge
 *     "A magnetic cartridge, more commonly called a phonograph cartridge or phono cartridge or (colloquially) a
 *     pickup, is an electromechanical transducer that is used to play records on a turntable."
 *   There will be two classes: AgentVideoPickup, AgentAudioPickup
 *   These two classes will be the "perspectives". Brainstorming ideas:
 *     AgentVideoPickup will act like a "needle" that can be placed in 2D coordinates in the World. The "needle"
 *     will check for (maybe without a Box2D body, maybe use getFixturesInAABB?) a RoomBox and set the screen view
 *     perspective accordingly.
 *     e.g. 1) "Video Needle"
 *         -a RoomBox exists for bounds box { x, y, w, h } = { 0, 0, 4, 4 }
 *             -RoomBox has property "screen y offset" = 1
 *             -remember, "screen y offset" is calculated starting at the bottom y coordinate of the RoomBox bounds
 *             -the absolute screen y position will be 1
 *         -a "video needle" is positioned at { 2, 2 }
 *         -the "video needle" must detect the preceding RoomBox, and use the property "screen y offset" to calculate
 *          a final screen view position of { 2, 1 }
 *      e.g. 2) "Audio Needle"
 *         -a RoomBox exists for bounds box { x, y, w, h } = { 0, 0, 4, 4 }
 *             -RoomBox has property "room music" = "Kid Icarus level 1 music"
 *         -a "audio needle" is positioned at { 2, 2 }
 *         -the "audio needle" must detect the preceding RoomBox, and use the property "room music" to store the
 *          currently playing room music
 *         -the class that is using the "audio needle" can poll the needle for current room music
 *
 * TODO add field for reference to owner Agency, to verify Agent
 */
public class Agent {
	public interface AgentUpdateListener { public void update(FrameTime frameTime); }
	public interface AgentDrawListener { public void draw(Eye eye); }

	DepNode removalNode;
	Collection<AgentRemovalListener> internalRemovalListeners;
	// listeners that this agent is using to listen for removal of another Agent
	Collection<AgentRemovalListener> myExternalRemovalListeners;
	// listeners that another agent is using to listen for removal of this Agent
	Collection<AgentRemovalListener> otherExternalRemovalListeners;
	Collection<AgentUpdateListener> updateListeners;
	Collection<AgentDrawListener> drawListeners;
	HashMap<String, AgentPropertyListener<?>> propertyListeners;
	Collection<String> globalPropertyKeys;
	HashSet<AgentBody> agentBodies;
	Object userData;

	Agent() {
		removalNode = null;
		internalRemovalListeners = new LinkedList<AgentRemovalListener>();
		myExternalRemovalListeners = new LinkedList<AgentRemovalListener>();
		otherExternalRemovalListeners = new LinkedList<AgentRemovalListener>();
		updateListeners = new LinkedList<AgentUpdateListener>();
		drawListeners = new LinkedList<AgentDrawListener>();
		propertyListeners = new HashMap<String, AgentPropertyListener<?>>();
		globalPropertyKeys = new LinkedList<String>();
		agentBodies = new HashSet<AgentBody>();
		userData = null;
	}

	// ignore warning because type safety is maintained by getClass().equals(cls)
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key, T defaultValue, Class<T> cls) {
		AgentPropertyListener<?> listener = propertyListeners.get(key);
		if(listener == null)
			return defaultValue;
		Object propValue = listener.getValue();
		// safety check for null value, and return null if found
		if(propValue == null)
			return null;
		// the class of the property value must be equal to, or a superclass of, cls - if not then throw error
		if(!cls.isAssignableFrom(propValue.getClass())) {
			throw new IllegalStateException("Unable to get Agent property=("+key+") because get class=("+
					cls.getName()+") doesn't equal property class=("+propValue.getClass().getName()+
					") for Agent=("+this+") and property value=("+propValue+")");
		}
		return (T) propValue;
	}

	// poll all property listeners and return result via object property list
	public ObjectProperties getAllProperties() {
		ObjectProperties props = new ObjectProperties();
		for(Entry<String, AgentPropertyListener<?>> iter : propertyListeners.entrySet())
			props.put(iter.getKey(), iter.getValue().getValue());
		return props;
	}

	public Object getUserData() {
		return userData;
	}
}
