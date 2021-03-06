package kidridicarus.common.role.tiledmap.solidlayer;

import java.util.Comparator;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.AgentFixture;
import kidridicarus.common.info.UInfo;

public class SolidLineSeg {
	// if begin = end, it means the LineSeg is one tile wide
	int begin;	// in tile coordinates (not pixel coordinates), where begin <= end
	int end;
	private final int otherOffset;
	AgentFixture agentFixture;

	public final boolean isHorizontal;
	/*
	 * Conceptually, each LineSeg has a 2D normal vector. "Up" means +y or +x as the case may be.
	 * Horizontal lines can be floors or ceilings. For horizontal lines:
	 *     If upNormal = true, this LineSeg is a floor, and the area below it is solid
	 *     If upNormal = false, this LineSeg is a ceiling, and the area above it is solid.
	 * Vertical lines can be left walls or right walls. For vertical lines:
	 *     If upNormal = true, this LineSeg is a left wall, and the area on its left is solid
	 *     If upNormal = false, this LineSeg is a right wall, and the area on its right is solid.
	 * Note: Variable is named "upNormal" instead of "rightNormal" to prevent confusion by way of left walls with
	 *       right normals.
	 */
	public final boolean upNormal;

	public SolidLineSeg(int begin, int end, int otherOffset, boolean isHorizontal, boolean upNormal) {
		if(begin > end)
			throw new IllegalArgumentException("Line segment begin > end exception, begin = " + begin + ", end = " + end);
		this.begin = begin;
		this.end = end;
		this.otherOffset = otherOffset;
		this.isHorizontal = isHorizontal;
		this.upNormal = upNormal;
		this.agentFixture = null;
	}

	/*
	 * If segA is completely to the left of segB, with no overlap, then return -1.
	 * If segA is completely to the right of segB, with no overlap, then return +1.
	 * If overlap exists then return 0.
	 * Overlap examples:
	 * -----------------
	 * E.g. 0)
	 *     If:
	 *         segA begins at 2 and ends at 4, and
	 *         segB begins at 4 and ends at 4
	 *     Then:
	 *         segA and segB overlap
	 *
	 * E.g. 1)
	 *     If:
	 *         segA begins at 2 and ends at 4, and
	 *         segB begins at 4 and ends at 10
	 *     Then:
	 *         segA and segB overlap
	 *
	 * E.g. 2)
	 *     If:
	 *         segA begins at 2 and ends at 4, and
	 *         segB begins at 5 and ends at 10
	 *     Then:
	 *         segA and segB do not overlap
	 *
	 * E.g. 3)
	 *     If:
	 *         segA begins at 2 and ends at 4, and
	 *         segB begins at 6 and ends at 10
	 *     Then:
	 *         segA and segB do not overlap
	 * etc.
	 * Assumption is made that horizontal lines will be compared only with horizontal lines and vertical lines
	 * will be compared only with vertical lines. Horizontal lines should be in separate list(s) from vertical
	 * lines.
	 * Although horizontal and vertical line segments cannot be compared, segments with upNormal = true can be
	 * compared to segments with upNormal = false.
	 */
	static class LineSegComparator implements Comparator<SolidLineSeg> {
		@Override
		public int compare(SolidLineSeg segA, SolidLineSeg segB) {
			if((segA.isHorizontal && !segB.isHorizontal) ||
				(!segA.isHorizontal && segB.isHorizontal)) {
				throw new IllegalArgumentException("Cannot compare horizontal and vertical line segments.");
			}
			if(segA.end < segB.begin)
				return -1;	// no overlap, segment A is left of segment B.
			else if(segA.begin > segB.end)
				return 1;	// no overlap, segment A is right of segment B.
			else
				return 0;	// overlap
		}
	}

	// epsilon test of this lineSeg's bounds against otherBounds
	public boolean dblCheckContact(Rectangle otherBounds) {
		Rectangle thisBounds = getBounds();
		// if horizontal and within width bounds, or vertical and within height bounds, then return contact true
		return (isHorizontal && thisBounds.x + thisBounds.width - UInfo.POS_EPSILON > otherBounds.x &&
				thisBounds.x + UInfo.POS_EPSILON < otherBounds.x + otherBounds.width) ||
			(!isHorizontal && thisBounds.y + thisBounds.height - UInfo.POS_EPSILON > otherBounds.y &&
				thisBounds.y + UInfo.POS_EPSILON < otherBounds.y + otherBounds.height);
	}

	public Rectangle getBounds() {
		if(isHorizontal) {
			return new Rectangle(UInfo.P2M(begin * UInfo.TILEPIX_X), UInfo.P2M(otherOffset * UInfo.TILEPIX_Y),
					UInfo.P2M((end+1-begin) * UInfo.TILEPIX_X), 0f);
		}
		else {
			return new Rectangle(UInfo.P2M(otherOffset * UInfo.TILEPIX_X), UInfo.P2M(begin * UInfo.TILEPIX_Y),
					0f, UInfo.P2M((end+1-begin) * UInfo.TILEPIX_Y));
		}
	}

	public String toString( ) {
		return "LineSeg: { begin="+begin+", end="+end+", isHorizontal="+isHorizontal+", upNormal="+upNormal+"}";
	}
}
