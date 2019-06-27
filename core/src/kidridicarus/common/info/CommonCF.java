package kidridicarus.common.info;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.tool.FilterBitSet;

/*
 * Common Contact Filter stuff
 * Overview:
 *   "Give Bit" and "Take Bit", or GIVEBIT and TAKEBIT:
 *   A simple solution for clarity of naming and "reading" contact filter bit sets, unfortunately involving twice as
 * many bits as is necessary in some cases. So that player and NPC fixtures can pass through each other while those
 * same fixtures are solid against a floor or ceiling or wall surface. Thus solid fixtures like map tile solid lines
 * are "two-way solids", while the player and NPC are "one-way" solids.
 *
 * Requirement 1) Implement the contact scheme as described by true/false matrix for 3 example solid types, so that
 *                players and NPCs can both "stand on the ground" (i.e. contact map tile solids), but those same
 *                players and NPCs can also pass through each other unimpeded.
 *   Acronym legend:
 *     STF = "two-way Solid" Tile map line Fixture
 *     SPF = "one-way Solid" Player Fixture
 *     SNF = "one-way Solid" Non-player character Fixture
 *     t = true
 *     f = false
 *   Contact matrix - shouldCollide contact test between row fixture and column fixture:
 *        STF   SPF   SNF
 *        --- | --- | ---
 *   STF | t  |  t  |  t
 *   SPF | t  |  f  |  f
 *   SNF | t  |  f  |  f
 *
 * Requirement 2) RoomBoxes can contact Players and NPCs, but the RoomBoxes cannot contact with other RoomBoxes -
 * thus reducing unnecessary contact counts.
 *
 * Solution:
 *   "Give" and "Take" Bit Paradigm:
 * Use the original Box2D contact filtering Category and Mask paradigm, and add a layer.
 * Each contact filter bit is actually two pieces: the "give" and the "take" - each bit is a pair that interacts with
 * each other.
 * Example for Requirement 1)
 *   One SolidLine fixture, one Player fixture, one NPC fixture.
 *     A) SolidLine fixture contact filter bits:
 *       Category bit set: { SOLID_GIVEBIT, SOLID_TAKEBIT }
 *       Mask bit set: { SOLID_TAKEBIT, SOLID_GIVEBIT }
 *     B) Player fixture contact filter bits:
 *       Category bit set: { SOLID_TAKEBIT }
 *       Mask bit set: { SOLID_GIVEBIT }
 *     C) NPC fixture contact filter bits:
 *       Category bit set: { SOLID_TAKEBIT }
 *       Mask bit set: { SOLID_GIVEBIT }
 *   If shouldCollide test is performed with fixtures A and B the result is true:
 *     CatA & MaskB = { SOLID_GIVEBIT, SOLID_TAKEBIT } & { SOLID_GIVEBIT } = { SOLID_GIVEBIT } = true
 *     CatB & MaskA = { SOLID_TAKEBIT } & { SOLID_TAKEBIT, SOLID_GIVEBIT } = { SOLID_TAKEBIT } = true
 *   If shouldCollide test is performed with fixtures A and C the result is true:
 *     CatA & MaskC = { SOLID_GIVEBIT, SOLID_TAKEBIT } & { SOLID_GIVEBIT } = { SOLID_GIVEBIT } = true
 *     CatC & MaskA = { SOLID_TAKEBIT } & { SOLID_TAKEBIT, SOLID_GIVEBIT } = { SOLID_TAKEBIT } = true
 *   If shouldCollide test is performed with fixtures B and C the result is false:
 *     CatB & MaskC = { SOLID_TAKEBIT } & { SOLID_GIVEBIT } = { } = false
 *     CatC & MaskB = { SOLID_TAKEBIT } & { SOLID_GIVEBIT } = { } = false
 *
 * Example for Requirement 2)
 *   Two RoomBox fixtures, one Player fixture, one NPC fixture.
 *     A) RoomBoxA fixture contact filter bits:
 *       Category bit set: { ROOM_GIVEBIT }
 *       Mask bit set: { ROOM_TAKEBIT }
 *     B) RoomBoxB fixture contact filter bits:
 *       Category bit set: { ROOM_GIVEBIT }
 *       Mask bit set: { ROOM_TAKEBIT }
 *     C) Player fixture contact filter bits:
 *       Category bit set: { ROOM_TAKEBIT }
 *       Mask bit set: { ROOM_GIVEBIT }
 *     D) NPC fixture contact filter bits:
 *       Category bit set: { ROOM_TAKEBIT }
 *       Mask bit set: { ROOM_GIVEBIT }
 *   If shouldCollide test is performed with fixtures A and B the result is false:
 *     CatA & MaskB = { ROOM_GIVEBIT } & { ROOM_TAKEBIT } = { } = false
 *     CatB & MaskA = { ROOM_GIVEBIT } & { ROOM_TAKEBIT } = { } = false
 *   If shouldCollide test is performed with fixtures A and C the result is true:
 *     CatA & MaskC = { ROOM_GIVEBIT } & { ROOM_GIVEEBIT } = { ROOM_GIVEBIT } = true
 *     CatC & MaskA = { ROOM_TAKEBIT } & { ROOM_TAKEBIT } = { ROOM_TAKEBIT } = true
 *   If shouldCollide test is performed with fixtures A and D the result is true:
 *     CatA & MaskD = { ROOM_GIVEBIT } & { ROOM_GIVEEBIT } = { ROOM_GIVEBIT } = true
 *     CatD & MaskA = { ROOM_TAKEBIT } & { ROOM_TAKEBIT } = { ROOM_TAKEBIT } = true
 *   If shouldCollide test is performed with fixtures B and C the result is true:
 *     CatB & MaskC = { ROOM_GIVEBIT } & { ROOM_GIVEEBIT } = { ROOM_GIVEBIT } = true
 *     CatC & MaskB = { ROOM_TAKEBIT } & { ROOM_TAKEBIT } = { ROOM_TAKEBIT } = true
 *   If shouldCollide test is performed with fixtures B and D the result is true:
 *     CatB & MaskD = { ROOM_GIVEBIT } & { ROOM_GIVEEBIT } = { ROOM_GIVEBIT } = true
 *     CatD & MaskB = { ROOM_TAKEBIT } & { ROOM_TAKEBIT } = { ROOM_TAKEBIT } = true
 *   If shouldCollide test is performed with fixtures C and D the result is false:
 *     CatC & MaskD = { ROOM_TAKEBIT } & { ROOM_GIVEBIT } = { } = false
 *     CatD & MaskC = { ROOM_TAKEBIT } & { ROOM_GIVEBIT } = { } = false
 *
 *   Fixture A's Category bit set includes the "give" version of the bit, and A's Mask bit set includes the "take"
 *   version of the bit.
 *   Fixture B's Category bit set includes the "take" version of the bit, and B's Mask bit set includes the "give"
 *   version of the bit.
 *   If a shouldCollide test is performed with fixture A and fixture B, then A and B will collide because
 *   A "gives/takes" to/from B the same bits that B "takes/gives" from/to A.
 *   Fixture A is the "original giver", such as a Player that "gives" a Player bit.
 *   Fixture B is the "original taker", such as an NPC that "takes" a Player bit.
 *   Thus the NPC can contact the Player without necessarily allowing contact between the NPC and other NPCs.
 *
 * Independence of AgentFilters:
 *   This paradigm allows for more independence among Agent/Role contact filter bits, because:
 * An Agent's contact filter can allow contact with another Agent without needing to modify the other Agent's
 * contact filter bits. The other Agent need only have an appropriate "give/take" contact filter bit pair.
 * However, one of the Agents must be a "type" that can "give" or "take" something. e.g. a fully solid object
 * can "give" and "take" solid contacts.
 * Short summary:
 *   More contact filter bits, more independence, same underlying contact filter scheme as Box2D.
 */
public class CommonCF {
	// Quick Contact Filter convenience class
	// filter bit set
	public static class QCF {
		public static FilterBitSet fbs(String... filterBitArray) { return new FilterBitSet(filterBitArray); }
	}

	// Agent Contact Filter Bit strings
	public class ACFB {
		public static final String SOLID_TILEMAP_GIVEBIT = "tiled_map_solid_givebit";
		public static final String SOLID_TILEMAP_TAKEBIT = "tiled_map_solid_takebit";
		public static final String SOLID_GIVEBIT = "solid_givebit";
		public static final String SOLID_TAKEBIT = "solid_takebit";
		// conditional solid is solid if a condition is met, e.g. one-way floors/platforms
		public static final String COND_SOLID_GIVEBIT = "cond_solid_givebit";
		public static final String COND_SOLID_TAKEBIT = "cond_solid_takebit";
		// To allow detectors (e.g. floor or "on-ground" detector) to always detect conditional solids (using
		// separate fixture to bypass the conditional's pre-solver).
		public static final String COND_SOLID_ALWAYS_GIVEBIT = "cond_solid_always_givebit";
		public static final String COND_SOLID_ALWAYS_TAKEBIT = "cond_solid_always_takebit";

		public static final String ROOM_GIVEBIT = "room_givebit";
		public static final String ROOM_TAKEBIT = "room_takebit";

		public static final String SPAWN_TRIGGER_GIVEBIT = "spawn_trigger_givebit";
		public static final String SPAWN_TRIGGER_TAKEBIT = "spawn_trigger_takebit";
		public static final String DESPAWN_TRIGGER_GIVEBIT = "despawn_trigger_givebit";
		public static final String DESPAWN_TRIGGER_TAKEBIT = "despawn_trigger_takebit";
		public static final String KEEP_ALIVE_TRIGGER_GIVEBIT = "keep_alive_trigger_givebit";
		public static final String KEEP_ALIVE_TRIGGER_TAKEBIT = "keep_alive_trigger_takebit";

		public static final String NPC_GIVEBIT = "npc_givebit";
		public static final String NPC_TAKEBIT = "npc_takebit";
		public static final String PLAYER_GIVEBIT = "player_givebit";
		public static final String PLAYER_TAKEBIT = "player_takebit";
		public static final String POWERUP_GIVEBIT = "powerup_givebit";
		public static final String POWERUP_TAKEBIT = "powerup_takebit";

		public static final String SCROLL_PUSH_GIVEBIT = "scroll_push_givebit";
		public static final String SCROLL_PUSH_TAKEBIT = "scroll_push_takebit";
	}

	public static final AgentFilter NO_CONTACT_FILTER = new AgentFilter();
	// This is a full solid, so:
	//   It gives what a solid gives and takes what a solid takes, and
	//   it takes what a solid gives and gives what a solid takes.
	public static final AgentFilter FULL_SOLID_FILTER = new AgentFilter(
			QCF.fbs(ACFB.SOLID_GIVEBIT, ACFB.SOLID_TAKEBIT),
			QCF.fbs(ACFB.SOLID_GIVEBIT, ACFB.SOLID_TAKEBIT));
	// This is a half solid, so:
	//   It only gives what a solid takes and takes what a solid gives.
	public static final AgentFilter HALF_SOLID_FILTER = new AgentFilter(ACFB.SOLID_TAKEBIT, ACFB.SOLID_GIVEBIT);
}
