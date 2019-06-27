package kidridicarus.common.role.tiledmap.drawlayer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agent.AgentDrawListener;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.DrawOrderAlias;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class DrawLayerRole extends Role {
	private TiledMapTileLayer drawLayer;

	public DrawLayerRole(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		drawLayer = properties.get(CommonKV.RoleMapParams.KEY_TILEDMAP_TILELAYER, null, TiledMapTileLayer.class);
		if(drawLayer == null)
			throw new IllegalArgumentException("TiledMapTileLayer not found in construction properties.");
		myAgentHooks.addDrawListener(getDrawOrderForLayer(drawLayer, CommonInfo.KIDRID_DRAWORDER_ALIAS),
				new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(drawLayer); }
			});
	}

	/*
	 * Returns draw order none if draw order not found for given layer,
	 * otherwise returns a draw order object based on the layer's draw order property.
	 */
	private Float getDrawOrderForLayer(TiledMapTileLayer layer, DrawOrderAlias[] drawOrderAliasList) {
		// does the layer contain a draw order key with a float value?
		Float drawOrderFloat = null;
		try {
			drawOrderFloat = layer.getProperties().get(CommonKV.Layer.KEY_LAYER_DRAWORDER, null, Float.class);
		}
		catch(ClassCastException poop) {
			// no float value, does the layer contain a draw order key with a string value?
			String drawOrderStr = null;
			try {
				drawOrderStr = layer.getProperties().get(CommonKV.Layer.KEY_LAYER_DRAWORDER, null, String.class);
			}
			catch(ClassCastException subPoop) {
				// return null because no float value and no string found to indicate draw order for layer
				return null;
			}
			// check draw order aliases to translate to draw order object
			return DrawOrderAlias.getDrawOrderForAlias(drawOrderAliasList, drawOrderStr);
		}
		return drawOrderFloat;
	}

	public static ObjectProperties makeRP(Rectangle bounds, TiledMapTileLayer layer) {
		ObjectProperties cmProps = RP_Tool.createRectangleRP(CommonKV.RoleClassAlias.VAL_DRAWABLE_TILEMAP, bounds);
		cmProps.put(CommonKV.RoleMapParams.KEY_TILEDMAP_TILELAYER, layer);
		return cmProps;
	}
}
