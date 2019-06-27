package kidridicarus.agency.tool;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

public interface Eye {
	public void setViewCenter(Vector2 viewCenter);
	public void draw(Sprite spr);
	public void draw(TiledMapTileLayer layer);
	public void begin();
	public void end();
	public boolean isDrawing();
	// it is a bit odd to create Stage here
	public Stage createStage();
}
