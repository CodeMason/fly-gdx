package de.fau.cs.mad.fly;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;

import de.fau.cs.mad.fly.features.IFeatureLoad;
import de.fau.cs.mad.fly.features.IFeatureRender;
import de.fau.cs.mad.fly.game.GameObject;

/**
 * Interface that has to implemented by everything that a user can steer in Fly.
 * 
 * @author Lukas Hahmann
 * 
 */
public interface IPlane extends IFeatureLoad, IFeatureRender, Disposable {
	public GameObject getInstance();
	public Model getModel();
	
	public float getSpeed();
	public float getAzimuthSpeed();
	public float getRollingSpeed();
}
