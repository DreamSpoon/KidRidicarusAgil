package kidridicarus.agency.tool;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

/*
 * A wrapper for the eye class, with getEye method that always returns non-null.
 * If no "real" eye is set for this eyePlug then incoming calls to draw, setViewCenter, etc. will be ignored.
 * Advantage: Code that uses getEye() in this way doesn't need to check if eye == null.
 */
public class EyePlug {
	private Eye fakeEye;
	private Eye realEye;

	public EyePlug() {
		realEye = null;
		// fake Eye will pass information to real Eye if real Eye exists
		fakeEye = new Eye() {
				@Override
				public void setViewCenter(Vector2 viewCenter) {
					if(realEye != null) realEye.setViewCenter(viewCenter);
				}
				@Override
				public void draw(Sprite spr) { if(realEye != null) realEye.draw(spr); }
				@Override
				public void draw(TiledMapTileLayer layer) { if(realEye != null) realEye.draw(layer); }
				@Override
				public void begin() { if(realEye != null) realEye.begin(); }
				@Override
				public void end() { if(realEye != null) realEye.end(); }
				@Override
				public boolean isDrawing() { return realEye != null ? realEye.isDrawing() : false; }
				@Override
				public Stage createStage() { return realEye != null ? realEye.createStage() : null; }
			};
	}

	public Eye getEye() {
		return fakeEye;
	}

	public void setEye(Eye eye) {
		realEye = eye;
	}
}
