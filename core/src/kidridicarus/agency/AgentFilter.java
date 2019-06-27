package kidridicarus.agency;

import kidridicarus.agency.tool.FilterBitSet;

/*
 *   Notes regarding give/take bits vs category/mask bits:
 * To reduce the number of unique bit strings needed to allow for contacts between:
 *   e.g. I) Solid and Solid Contact
 *     3 bodies, with one fixture per body; A, B, C have this contact filter bits configuration:
 *       giveBitsA = SOLID_BIT
 *       takeBitsA = SOLID_BIT
 *       giveBitsB = SOLID_BIT
 *       takeBitsB = SOLID_BIT
 *       giveBitsC = SOLID_BIT
 *       takeBitsC = SOLID_BIT
 *     The same bits combo, so they will all collide with each other. A collides with B, and B collides with C, etc.
 *
 * Separate example:
 *   e.g. II) Solid and One-way Solid Contact
 *     3 bodies, with one fixture per body; A and B and C have this contact filter bits configuration:
 *       giveBitsA = SOLID_BIT
 *       takeBitsA = SOLID_BIT
 *       giveBitsB = (no bits)
 *       takeBitsB = SOLID_BIT
 *       giveBitsC = (no bits)
 *       takeBitsC = SOLID_BIT
 *     Body A is totally solid and can contact body B and body C.
 *     However, bodies B and C will not contact each other because they can only "receive" bits and not "give" bits.
 *     Whether B and C are both "giving" or both "receiving" is an interesting side-path to explore. This contact
 *     filtering situation is intended to allow a player and NPC scenario where: the player and NPC are both standing
 *     on (contacting) a solid floor, but the player body and NPC bodies should be able to pass through each other. 
 *     Conceptually, the "totally solid" line that represents a map's solid line boundary is both "giving" and
 *     "receiving" solid contacts with other bodies. The player body and NPC bodies are only "receiving" contacts,
 *     thus player and NPC cannot contact.
 */
public class AgentFilter {
	public FilterBitSet catBits;
	public FilterBitSet maskBits;

	public AgentFilter() {
		// default to no contact
		this.catBits = new FilterBitSet();
		this.maskBits = new FilterBitSet();
	}

	public AgentFilter(FilterBitSet catBits, FilterBitSet maskBits) {
		this.catBits = catBits;
		this.maskBits = maskBits;
	}

	public AgentFilter(String catBit, String maskBit) {
		this(new FilterBitSet(catBit), new FilterBitSet(maskBit));
	}

	public void set(AgentFilter otherFilter) {
		this.catBits = otherFilter.catBits;
		this.maskBits = otherFilter.maskBits;
	}

	public void set(FilterBitSet catBits, FilterBitSet maskBits) {
		this.catBits = catBits;
		this.maskBits = maskBits;
	}

	public void set(String catBit, String maskBit) {
		set(new FilterBitSet(catBit), new FilterBitSet(maskBit));
	}

	public static boolean shouldCollide(AgentFilter filterA, AgentFilter filterB) {
		return filterA.catBits.and(filterB.maskBits).isNonZero() &&
				filterB.catBits.and(filterA.maskBits).isNonZero();
	}
}
