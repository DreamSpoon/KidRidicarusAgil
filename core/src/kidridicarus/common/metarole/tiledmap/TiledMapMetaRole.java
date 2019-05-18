package kidridicarus.common.metarole.tiledmap;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metarole.tiledmap.drawlayer.DrawLayerRole;
import kidridicarus.common.metarole.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.info.StoryKV;

/*
 * A "parent" or "meta" Role that has a solid tile map, drawable layers, and a batch of initial spawn Roles.
 * This Role does not load anything from file, rather it is given a TiledMap object that has been preloaded.
 */
public class TiledMapMetaRole extends Role implements Disposable {
	private TiledMap map;
	private SolidTiledMapRole solidTileMapRole;
	private LinkedList<DrawLayerRole> drawLayerRoles;
	private LinkedList<Disposable> manualDisposeRoles;
	private AgentUpdateListener myUpdateListener;

	public TiledMapMetaRole(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		manualDisposeRoles = new LinkedList<Disposable>();

		map = properties.get(CommonKV.RoleMapParams.KEY_TILEDMAP, null, TiledMap.class);
		if(map == null)
			throw new IllegalArgumentException("Tiled map property not set, unable to create Role.");
		createInitialSubRoles(RP_Tool.getBounds(properties));

		// keep ref to update listener for removal
		myUpdateListener = new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { doUpdate(); }
			};
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, myUpdateListener);
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	// create the Roles for the solid tile map and the drawable layers
	private void createInitialSubRoles(Rectangle bounds) {
		// get lists of solid and draw layers (there may be overlap between the two, that's okay)
		LinkedList<TiledMapTileLayer> solidLayers = new LinkedList<TiledMapTileLayer>(); 
		LinkedList<TiledMapTileLayer> drawLayers = new LinkedList<TiledMapTileLayer>(); 
		for(MapLayer layer : map.getLayers()) {
			if(!(layer instanceof TiledMapTileLayer))
				continue;

			// is solid layer property set to true?
			if(layer.getProperties().get(CommonKV.Layer.KEY_LAYER_SOLID,
					CommonKV.VAL_FALSE, String.class).equals(CommonKV.VAL_TRUE)) {
				solidLayers.add((TiledMapTileLayer) layer);
			}
			// does this layer have a draw order?
			if(layer.getProperties().get(CommonKV.Layer.KEY_LAYER_DRAWORDER, null, String.class) != null)
				drawLayers.add((TiledMapTileLayer) layer);
		}

		createSolidTileMapRole(bounds, solidLayers);
		createDrawLayerRoles(drawLayers);
	}

	private void createSolidTileMapRole(Rectangle bounds, LinkedList<TiledMapTileLayer> solidLayers) {
		if(solidLayers.isEmpty())
			return;

		solidTileMapRole = (SolidTiledMapRole) this.myStoryHooks.createRole(
				SolidTiledMapRole.makeRP(bounds, solidLayers));
	}

	private void createDrawLayerRoles(LinkedList<TiledMapTileLayer> drawLayers) {
		if(drawLayers.isEmpty())
			return;
		drawLayerRoles = new LinkedList<DrawLayerRole>();
		// loop through each draw layer in the list, adding each and passing a ref to its tiled map layer
		for(TiledMapTileLayer layer : drawLayers) {
			drawLayerRoles.add((DrawLayerRole) myStoryHooks.createRole(DrawLayerRole.makeRP(
					myAgent.getProperty(CommonKV.KEY_BOUNDS, null, Rectangle.class), layer)));
		}
	}

	/*
	 * Create other spawn Roles on first update because the solid stuff in the map will have been created:
	 *   -when the Story was created, before any update frames have been run, the map Role was created
	 *   -in the previous update
	 * So there was previously no solid stuff for the initial spawn Roles to land on, so they needed to be
	 * spawned later.
	 * Note: Only one update, then update listener is removed.
	 */
	private void doUpdate() {
		myAgentHooks.removeUpdateListener(myUpdateListener);
		createOtherRoles();
	}

	/*
	 * Non-disposable Roles (e.g. KeepAliveBox) may be created by this Role, so keep a list of Roles for
	 * manual disposal.
	 */
	private void createOtherRoles() {
		// create the other Roles (typically spawn boxes, rooms, player start, etc.)
		List<Role> createdRoles = myStoryHooks.createRoles(makeRolePropsFromLayers(map.getLayers()));
		for(Role role : createdRoles) {
			if(role instanceof Disposable)
				manualDisposeRoles.add((Disposable) role);
		}
	}

	private LinkedList<ObjectProperties> makeRolePropsFromLayers(MapLayers layers) {
		LinkedList<ObjectProperties> roleProps = new LinkedList<ObjectProperties>();
		for(MapLayer layer : layers)
			roleProps.addAll(makeRolePropsFromLayer(layer));
		return roleProps;
	}

	private LinkedList<ObjectProperties> makeRolePropsFromLayer(MapLayer layer) {
		if(layer instanceof TiledMapTileLayer)
			return makeRolePropsFromTileLayer((TiledMapTileLayer) layer);
		else
			return makeRolePropsFromObjLayer(layer);
	}

	private LinkedList<ObjectProperties> makeRolePropsFromTileLayer(TiledMapTileLayer tiledMapLayer) {
		LinkedList<ObjectProperties> roleProps = new LinkedList<ObjectProperties>();
		// if cannot find a valid Role class alias then return empty list
		String roleClassAlias = tiledMapLayer.getProperties().get(StoryKV.KEY_ROLE_CLASS, "", String.class);
		if(!myStoryHooks.isValidRoleClassAlias(roleClassAlias))
			return roleProps;
		// create list of RoleProperties objects with some tile info and info from the layer's MapProperties
		for(int y=0; y<tiledMapLayer.getHeight(); y++) {
			for(int x=0; x<tiledMapLayer.getWidth(); x++) {
				// only spawn a Role if the cell exists
				if(tiledMapLayer.getCell(x, y) == null || tiledMapLayer.getCell(x, y).getTile() == null)
					continue;
				roleProps.add(RP_Tool.createTileRP(tiledMapLayer.getProperties(), UInfo.RectangleT2M(x, y),
						tiledMapLayer.getCell(x,  y).getTile().getTextureRegion()));
			}
		}
		return roleProps;
	}

	private LinkedList<ObjectProperties> makeRolePropsFromObjLayer(MapLayer layer) {
		LinkedList<ObjectProperties> roleProps = new LinkedList<ObjectProperties>();
		for(RectangleMapObject rect : layer.getObjects().getByType(RectangleMapObject.class)) {
			// combine the layer and object properties and pass to the Role properties creator
			MapProperties combined = new MapProperties();
			combined.putAll(layer.getProperties());
			combined.putAll(rect.getProperties());

			// only spawn if can find a valid Role class alias
			String roleClassAlias = combined.get(StoryKV.KEY_ROLE_CLASS, "", String.class);
			if(myStoryHooks.isValidRoleClassAlias(roleClassAlias))
				roleProps.add(RP_Tool.createRectangleRP(combined, UInfo.RectangleP2M(rect.getRectangle())));
		}
		return roleProps;
	}

	@Override
	public void dispose() {
		for(Disposable role: manualDisposeRoles)
			role.dispose();
		if(solidTileMapRole != null)
			solidTileMapRole.dispose();
		if(map != null)
			map.dispose();
		// Draw layer Roles do not need to be disposed (currently) because they do not release gfx memory,
		// (the tilemap releases the memory) - and the draw layer Roles do not have Box2D bodies (currently).
	}

	public static ObjectProperties makeRP(TiledMap tiledMap) {
		int width = tiledMap.getProperties().get(CommonKV.TiledMap.KEY_WIDTH, 0, Integer.class);
		int height = tiledMap.getProperties().get(CommonKV.TiledMap.KEY_HEIGHT, 0, Integer.class);
		if(width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Cannot create map Role from tiledMap when width or height" +
					"is not positive: width = " + width + ", height = " + height);
		}
		ObjectProperties props = RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_META_TILEDMAP,
				new Rectangle(0f, 0f, UInfo.P2M(width * UInfo.TILEPIX_X), UInfo.P2M(height * UInfo.TILEPIX_Y)));
		props.put(CommonKV.RoleMapParams.KEY_TILEDMAP, tiledMap);
		return props;
	}
}
