package com.ridicarus.kid.collisionmap;

import java.util.Iterator;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.tiles.BooleanTileMap;

public class TileCollisionMap {
	private World world;
	private BooleanTileMap bTileMap;

	private int width;
	private int height;
	private int tileWidth;
	private int tileHeight;
	private LineSegList[] hLines;
	private LineSegList[] vLines;

	// 1) width and height are given in number of tiles (not number of pixels).
	// 2) tileWidth and tileHeight are given in pixels.
	public TileCollisionMap(World world, TiledMapTileLayer layer, int width, int height, int tileWidth, int tileHeight) {
		this.world = world;
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;

		hLines = new LineSegList[height+1];
		for(int i=0; i<=height; i++)
			hLines[i] = new LineSegList();

		vLines = new LineSegList[width+1];
		for(int i=0; i<=width; i++)
			vLines[i] = new LineSegList();

		bTileMap = new BooleanTileMap(layer);
		calculateLineSegs();
		createBodies();
	}

	// Create a minimal line segment based boundary set based on the tile map input - for collision geometry
	// creation/tracking/removal.
	// Use one dimensional integer line segments.
	// The goal: Each non-empty (non-null) tile in the map layer will be surrounded by bounding lines (a bounding
	// rectangle).
	// The problems:
	//     1) Irrelevant lines may be created (e.g. where two bricks are side by side, the overlapping/coincident
	//        lines should be erased).
	//     2) Parallel lines that share vertexes and have the same "upNormal" need to be fused together, to reduce
	//        the total number of line segments without changing the physical boundaries.
	//
	// Brute force solution:
	//     Loop through all non-empty tiles and create boxes for each, then remove unnecessary lines and fuse lines
	//     where possible. This may necessitate something like a quadtree to track all the lines, and that's so
	//     annoying to implement.
	//
	// Another solution:
	//     Think of the problem as: How to create the minimum amount of of horizontal line segments, and vertical line
	//     segments, to represent the boundaries of the non-empty tiles.
	//     The horizontal lines, although crossing the vertical lines, are not affected by the vertical lines. That is,
	//     the calculation of the horizontal lines is independent of the calculation of the vertical lines.
	//     Since the horizontal lines are evenly spaced on the y-axis, it's simple to add them to an array/tree
	//     structure for the y-dimension, then for each row create an array/tree to store the line segments on the
	//     x-dimension. The lines are non-overlapping but may be adjacent (e.g. a ceiling is adjacent to a floor).
	//
	//     So,
	//         1) Loop through the tiles from left-to-right, to create the horizontal lines.
	//         2) Loop through the tiles from bottom-to-top, to create the vertical lines.
	//     Caveat: This algorithm is designed specifically with a regular tileset of same sized rectangles in mind - every
	//         tile must be the same size as every other tile.
	//         Other shapes may not be compatible, and may be added later using a different algorithm (?).
	private void calculateLineSegs() {
		LineSeg currentSeg;

		// Create horizontal lines.
		// Note the less than or equal (<=) compare on the 'y' iterator only.
		// In each iteration of the loop, the bottom of each tile is checked for line creation - but not the top.
		// This method was chosen to simplify things and work on one line at a time.
		// Therefore an extra iteration is needed to check the top line of the final tile (which is the bottom line
		// of the top row of tiles).
		currentSeg = null;
		for(int y = 0; y <= bTileMap.getHeight(); y++) {
			for(int x = 0; x < bTileMap.getWidth(); x++) {
				boolean me = bTileMap.gracefulGetCell(x, y);
				boolean belowMe = bTileMap.gracefulGetCell(x, y-1);

				// If the current tile and the one below it are both full (non-empty) then no line segment is needed
				// since the tiles are adjacent and a line segment would be redundant.
				// If the current tile and the tile below it are both empty then no line segment is
				// needed because there is just empty space.
				if((me && belowMe) || (!me && !belowMe)) {
					if(currentSeg != null) {
						// no segment needed for this tile, but a line segment has already been started...
						// finish the current segment and add it to the list (each y value has it's own list)
						currentSeg.end = x-1;
						hLines[y].add(currentSeg);

						currentSeg = null;
					}
				}
				else {	// create/continue line segment
					// if me is empty and belowMe is not empty, then this segment is a floor, therefore upNormal = true 
					boolean upNormal;
					upNormal = (!me && belowMe);
					// start a new segment if none currently exists
					if(currentSeg == null)
						currentSeg = new LineSeg(x, x, true, upNormal);
					// end the current segment and start a new one if the upNormal changed
					else if(upNormal != currentSeg.upNormal) {
						currentSeg.end = x-1;
						hLines[y].add(currentSeg);

						// start new segment with new upNormal
						currentSeg = new LineSeg(x, x, true, upNormal);
					}
				}
			}

			// at the end of the x axis check, if a line segment exists, then add to list and reset current
			if(currentSeg != null) {
				hLines[y].add(currentSeg);
				currentSeg = null;
			}
		}

		// Create vertical lines.
		currentSeg = null;
		for(int x = 0; x <= bTileMap.getWidth(); x++) {
			for(int y = 0; y < bTileMap.getHeight(); y++) {
				boolean me = bTileMap.gracefulGetCell(x, y);
				boolean leftOfMe = bTileMap.gracefulGetCell(x-1, y);

				// If the current tile and the one left of it are both full (non-empty) then no line segment is needed
				// since the tiles are adjacent and a line segment would be redundant.
				// If the current tile and the tile left of it are both empty then no line segment is
				// needed because there is just empty space.
				if((me && leftOfMe) || (!me && !leftOfMe)) {
					if(currentSeg != null) {
						// no segment needed for this tile, but a line segment has already been started...
						// finish the current segment and add it to the list (each y value has it's own list)
						currentSeg.end = y-1;
						vLines[x].add(currentSeg);

						currentSeg = null;
					}
				}
				else {	// create/continue line segment
					// if me is empty and leftOfMe is not empty, then this segment is a left wall, therefore upNormal = true
					// (the normal points to the right) 
					boolean upNormal;
					upNormal = (!me && leftOfMe);
					// start a new segment if none currently exists
					if(currentSeg == null)
						currentSeg = new LineSeg(y, y, false, upNormal);
					// end the current segment and start a new one if the upNormal changed
					else if(upNormal != currentSeg.upNormal) {
						currentSeg.end = y-1;
						vLines[x].add(currentSeg);

						// start new segment with new upNormal
						currentSeg = new LineSeg(y, y, false, upNormal);
					}
				}
			}

			// at the end of the y axis check, if a line segment exists, then add to list and reset current
			if(currentSeg != null) {
				vLines[x].add(currentSeg);
				currentSeg = null;
			}
		}
	}

	// create Box2d bodies for the horizontal and vertical line segments
	private void createBodies() {
		// loop through rows
		for(int y=0; y<=height; y++) {
			// loop through row's line segments
			Iterator<LineSeg> segIter = hLines[y].getIterator();
			while(segIter.hasNext()) {
				LineSeg seg = segIter.next();
				seg.body = defineHLineBody(y, seg);
			}
		}

		// loop through columns
		for(int x=0; x<=width; x++) {
			// loop through row's line segments
			Iterator<LineSeg> segIter = vLines[x].getIterator();
			while(segIter.hasNext()) {
				LineSeg seg = segIter.next();
				seg.body = defineVLineBody(x, seg);
			}
		}
	}

	private Body defineHLineBody(int yRow, LineSeg seg) {
		// Add +1 to end, because the line segment ends on the right side of end.
		// Consider the case where the segment is one wide. Then seg.begin would equal seg.end.
		// So we need to add one.
		return defineLineBody(seg.begin, yRow, seg.end+1, yRow, seg);
	}

	private Body defineVLineBody(int xCol, LineSeg seg) {
		return defineLineBody(xCol, seg.begin, xCol, seg.end+1, seg);
	}

	private Body defineLineBody(int startX, int startY, int endX, int endY, LineSeg seg) {
		BodyDef bdef;
		FixtureDef fdef;
		EdgeShape edgeShape;
		Body body;

		bdef = new BodyDef();
		bdef.position.set(GameInfo.P2M(startX * tileWidth), GameInfo.P2M(startY * tileHeight));
		bdef.type = BodyDef.BodyType.StaticBody;
		body = world.createBody(bdef);

		fdef = new FixtureDef();
		edgeShape = new EdgeShape();
		edgeShape.set(0f, 0f, GameInfo.P2M((endX - startX) * tileWidth), GameInfo.P2M((endY - startY) * tileHeight));
		fdef.filter.categoryBits = GameInfo.BOUNDARY_BIT;
//		fdef.filter.maskBits = ...

		fdef.shape = edgeShape;
		body.createFixture(fdef).setUserData(seg);

		return body;
	}

	/*
	 * Returns true if:
	 *   1) (x, y) is within the boundaries of the tile map, and
	 *   2) tile exists at (x, y)
	 * Returns false otherwise.
	 */
	public boolean isTileExist(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y > height)
			return false;
		return bTileMap.getCell(x, y);
	}

	public void addTile(int x, int y) {
		if(isTileExist(x, y)) {
			throw new IllegalStateException("Cannot add tile at (x, y) = (" + x + ", " + y +
					") since tile already exists.");
		}

		// if the tile on the right exists then remove the lineSeg since the tiles are contiguous
		if(bTileMap.gracefulGetCell(x+1, y))
			removeVLineSegment(x+1, y);
		// otherwise create a right wall lineSeg
		else
			addVLineSegment(x+1, y, true);

		// if the tile on the left exists then remove the lineSeg since the tiles are contiguous
		if(bTileMap.gracefulGetCell(x-1, y))
			removeVLineSegment(x, y);
		// otherwise create a left wall lineSeg
		else
			addVLineSegment(x, y, false);

		// if the tile above exists then remove the lineSeg since the tiles are contiguous
		if(bTileMap.gracefulGetCell(x, y+1))
			removeHLineSegment(x, y+1);
		// otherwise create a floor lineSeg
		else
			addHLineSegment(x, y+1, true);

		// if the tile below exists then remove the lineSeg since the tiles are contiguous
		if(bTileMap.gracefulGetCell(x, y-1))
			removeHLineSegment(x, y);
		// otherwise create a ceiling lineSeg
		else
			addHLineSegment(x, y, false);

		// finally, record that the tile is solid in the boolean tile map
		bTileMap.setCell(x, y, true);
	}

	public void removeTile(int x, int y) {
		if(!isTileExist(x, y)) {
			throw new IllegalStateException("Cannot remove tile at (x, y) = (" + x + ", " + y +
					") since tile does not exist.");
		}

		// if the tile on the right exists then create a right wall lineSeg, since it is solid on the right
		if(bTileMap.gracefulGetCell(x+1, y))
			addVLineSegment(x+1, y, false);
		// otherwise remove the lineSeg since both tiles are empty, no collision possible
		else
			removeVLineSegment(x+1, y);

		// if the tile on the left exists then create a left wall lineSeg, since it is solid on the left
		if(bTileMap.gracefulGetCell(x-1, y))
			addVLineSegment(x, y, true);
		// otherwise remove the lineSeg since both tiles are empty, no collision possible
		else
			removeVLineSegment(x, y);

		// if the tile above exists then create a ceiling lineSeg, since it is solid above
		if(bTileMap.gracefulGetCell(x, y+1))
			addHLineSegment(x, y+1, false);
		// otherwise remove the lineSeg since both tiles are empty, no collision possible
		else
			removeHLineSegment(x, y+1);

		// if the tile below exists then create a floor lineSeg, since it is solid below
		if(bTileMap.gracefulGetCell(x, y-1))
			addHLineSegment(x, y, true);
		// otherwise remove the lineSeg since both tiles are empty, no collision possible
		else
			removeHLineSegment(x, y);

		// finally, record that the tile is empty in the boolean tile map
		bTileMap.setCell(x, y, false);
	}

	private void addHLineSegment(int x, int y, boolean upNormal) {
		addLineSegment(hLines, true, x, y, upNormal);
	}
	private void addVLineSegment(int x, int y, boolean upNormal) {
		addLineSegment(vLines, false, y, x, upNormal);
	}
	private void addLineSegment(LineSegList[] horvLines, boolean isHorizontal, int x, int y, boolean upNormal) {
		LineSeg newSeg;
		LineSeg floorSeg;
		LineSeg higherSeg;

		newSeg = new LineSeg(x, x, isHorizontal, upNormal);
		// floor gives <= x
		// (usually overlap may occur, but since we are adding, the overlap is assumed to be impossible)
		floorSeg = horvLines[y].lineSegs.floor(newSeg);
		// higher gives > x
		// (no possibility of overlap)
		higherSeg = horvLines[y].lineSegs.higher(newSeg);

		// adjacency on right? 
		if(higherSeg != null && higherSeg.begin == x+1) {
			// if the upNormal of the right seg matches the new seg then join with right seg
			if(higherSeg.upNormal == upNormal) {
				newSeg.end = higherSeg.end;
				// destroy old right segment
				world.destroyBody(higherSeg.body);
				horvLines[y].remove(higherSeg);
			}
		}
		// adjacency on left?
		if(floorSeg != null && floorSeg.end == x-1) {
			// if the upNormal of the left seg matches the new seg then join with left seg
			if(floorSeg.upNormal == upNormal) {
				newSeg.begin = floorSeg.begin;
				// destroy old left segment
				world.destroyBody(floorSeg.body);
				horvLines[y].remove(floorSeg);
			}
		}

		if(isHorizontal)
			newSeg.body = defineHLineBody(y, newSeg);
		else
			newSeg.body = defineVLineBody(y, newSeg);
		horvLines[y].add(newSeg);
	}

	private void removeHLineSegment(int x, int y) {
		removeLineSegment(hLines, true, x, y);
	}
	private void removeVLineSegment(int x, int y) {
		removeLineSegment(vLines, false, y, x);
	}
	private void removeLineSegment(LineSegList[] horvLines, boolean isHorizontal, int x, int y) {
		LineSeg testSeg;
		LineSeg floorSeg;
		LineSeg newSeg;
		int leftBegin;
		int leftEnd;
		int rightBegin;
		int rightEnd;

		// upNormal is arbitrary
		testSeg = new LineSeg(x, x, isHorizontal, false);
		// floor gives <= x
		// (usually overlap may occur, but since we are adding, the overlap is assumed to be impossible)
		floorSeg = horvLines[y].lineSegs.floor(testSeg);

		// if there were no line segments overlapping x, then we cannot remove 
		if(floorSeg == null || floorSeg.end < x)
			throw new IllegalStateException("Cannot remove line segment (or portion thereof) that does not exist.");

		// destroy the original lineSeg, and new segments might be created later
		world.destroyBody(floorSeg.body);
		horvLines[y].remove(floorSeg);

		leftBegin = floorSeg.begin;
		leftEnd = x-1;
		rightBegin = x+1;
		rightEnd = floorSeg.end;
		// need to create new lineSeg on left?
		if(leftBegin <= leftEnd) {
			newSeg = new LineSeg(leftBegin, leftEnd, floorSeg.isHorizontal, floorSeg.upNormal);
			if(isHorizontal)
				newSeg.body = defineHLineBody(y, newSeg);
			else
				newSeg.body = defineVLineBody(y, newSeg);
			horvLines[y].add(newSeg);
		}
		// need to create new lineSeg on right?
		if(rightBegin <= rightEnd) {
			newSeg = new LineSeg(rightBegin, rightEnd, floorSeg.isHorizontal, floorSeg.upNormal);
			if(isHorizontal)
				newSeg.body = defineHLineBody(y, newSeg);
			else
				newSeg.body = defineVLineBody(y, newSeg);
			horvLines[y].add(newSeg);
		}
	}

	public void dispose() {
		Iterator<LineSeg> iter;
		if(hLines != null) {
			// destroy any B2 bodies attached to line segments
			for(int y=0; y<height+1; y++) {
				iter = hLines[y].getIterator();
				while(iter.hasNext())
					iter.next().dispose();
			}
		}
		if(vLines != null) {
			// destroy any B2 bodies attached to line segments
			for(int x=0; x<width+1; x++) {
				iter = vLines[x].getIterator();
				while(iter.hasNext())
					iter.next().dispose();
			}
		}
	}
}
