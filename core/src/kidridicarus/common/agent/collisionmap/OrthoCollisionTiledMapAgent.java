package kidridicarus.common.agent.collisionmap;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.collisionmap.stuff.OrthoTileCollisionMap;
import kidridicarus.common.agentbody.general.OrthoTiledMapBody;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;

public class OrthoCollisionTiledMapAgent extends Agent implements Disposable {
	private OrthoTileCollisionMap otcMap;
	private OrthoTiledMapBody tmBody;
	private LinkedBlockingQueue<TileChange> tileChangeQ;

	@SuppressWarnings("unchecked")
	public OrthoCollisionTiledMapAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		Collection<TiledMapTileLayer> solidLayers =
				properties.get(CommonKV.AgentMapParams.KEY_TILEDMAPTILELAYER_LIST, null, Collection.class);
		if(solidLayers == null || solidLayers.isEmpty())
			throw new IllegalArgumentException("Layers array was null or empty.");
		// changes are made in batches, so keep a queue of pending changes
		tileChangeQ = new LinkedBlockingQueue<TileChange>();
		// The OTC map will create bodies with the World...
		otcMap = new OrthoTileCollisionMap(agency.getWorld(), solidLayers);
		// ... and a meta-body will be created that encompasses all the solid bound line bodies.
		tmBody = new OrthoTiledMapBody(this, agency.getWorld(), Agent.getStartBounds(properties));
		// Agent has post update because:
		//   -it may receive requests to modify the solid state of tiles inside itself during the regular Agency update
		//   -it will process these requests in batch format during post-update
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.POST_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
	}

	private void doUpdate(float delta) {
		// process change q
		while(!tileChangeQ.isEmpty())
			doChange(tileChangeQ.poll());
	}

	private void doChange(TileChange change) {
		if(change.solid) {
			if(otcMap.isTileExist(change.x, change.y)) {
				throw new IllegalStateException(
						"Cannot add solid tile where solid tile already exists in collision map.");
			}
			otcMap.addTile(change.x, change.y);
		}
		// change from to solid non-solid
		else {
			if(!otcMap.isTileExist(change.x, change.y)) {
				throw new IllegalStateException(
						"Cannot remove solid tile where solid tile does not already exist in collision map.");
			}
			otcMap.removeTile(change.x, change.y);
		}
	}

	/*
	 * Input is in the regular world coordinates. The position will be converted to tile coordinates, then
	 * the tile solid state will be set using the tile coordinates.
	 */
	public void setTileSolidStateAtPos(Vector2 position, boolean solid) {
		Vector2 tileCoords = UInfo.getM2PTileForPos(position);
		tileChangeQ.add(new TileChange((int) tileCoords.x, (int) tileCoords.y, solid));
	}

	public boolean isMapTileSolid(Vector2 tileCoords) {
		return otcMap.isTileExist((int) tileCoords.x, (int) tileCoords.y);
	}

	public boolean isMapPointSolid(Vector2 position) {
		// convert the point position to tile coordinates
		Vector2 tileCoords = UInfo.getM2PTileForPos(position);
		return otcMap.isTileExist((int) tileCoords.x, (int) tileCoords.y);
	}

	@Override
	public Vector2 getPosition() {
		return tmBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return tmBody.getBounds();
	}

	@Override
	public void dispose() {
		otcMap.dispose();
		tmBody.dispose();
	}
}